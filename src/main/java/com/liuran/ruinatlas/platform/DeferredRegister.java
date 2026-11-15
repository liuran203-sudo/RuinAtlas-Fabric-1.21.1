package com.liuran.ruinatlas.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * 注册表装配入口：与 NeoForge 的 DeferredRegister 同形，落到 Fabric 上
 * <p>Fabric 没有延迟注册的 mod 事件总线，因此静态字段初始化时即直接写入注册表。
 * {@link #register()} 保留为与 NeoForge 版一致的统一装配点，用于显式触发所属类装载。
 * <p>装配顺序由主类 {@code RuinAtlas} 显式驱动：方块 → 物品 → 粒子 → 数据附加 → 创造栏。
 */
public class DeferredRegister<T> {
	protected final Registry<T> registry;
	protected final String namespace;
	protected final List<DeferredHolder<T, ? extends T>> entries = new ArrayList<>();

	protected DeferredRegister(Registry<T> registry, String namespace) {
		this.registry = registry;
		this.namespace = namespace;
	}

	public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
		return new DeferredRegister<>(registryOf(key), namespace);
	}

	/** 方块注册表入口，与 NeoForge 的 DeferredRegister.createBlocks 同名 */
	public static Blocks createBlocks(String namespace) {
		return Blocks.createBlocks(namespace);
	}

	/** 物品注册表入口，与 NeoForge 的 DeferredRegister.createItems 同名 */
	public static Items createItems(String namespace) {
		return Items.createItems(namespace);
	}

	@SuppressWarnings("unchecked")
	protected static <T> Registry<T> registryOf(ResourceKey<? extends Registry<T>> key) {
		Registry<?> raw = BuiltInRegistries.REGISTRY.get(key.location());

		if (raw == null) {
			throw new IllegalStateException("注册表不存在：" + key.location());
		}

		return (Registry<T>) raw;
	}

	public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> factory) {
		return this.registerValue(name, factory.get());
	}

	/** 真正写入注册表并留下句柄的唯一入口 */
	protected <I extends T> DeferredHolder<T, I> registerValue(String name, I value) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.namespace, name);
		Registry.register(this.registry, id, value);

		DeferredHolder<T, I> holder = new DeferredHolder<>(id, value);
		this.entries.add(holder);
		return holder;
	}

	/** 已登记的全部注册项，供创造栏与数据生成遍历 */
	public List<DeferredHolder<T, ? extends T>> getEntries() {
		return Collections.unmodifiableList(this.entries);
	}

	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * 与 NeoForge 版保持一致的装配入口
	 * <p>字段初始化已经完成注册，这里只保证所属类被装载，不再排队注册。
	 */
	public void register() {
		// 注册已在字段初始化阶段完成，无需额外动作
	}

	/**
	 * 方块注册表
	 */
	public static class Blocks extends DeferredRegister<Block> {
		protected Blocks(Registry<Block> registry, String namespace) {
			super(registry, namespace);
		}

		public static Blocks createBlocks(String namespace) {
			return new Blocks(registryOf(Registries.BLOCK), namespace);
		}

		public <I extends Block> DeferredBlock<I> registerBlock(String name, Function<Properties, ? extends I> factory, Properties props) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.namespace, name);
			I value = factory.apply(props);
			Registry.register(this.registry, id, value);

			DeferredBlock<I> holder = new DeferredBlock<>(id, value);
			this.entries.add(holder);
			return holder;
		}

		public <I extends Block> DeferredBlock<I> registerBlock(String name, Supplier<? extends I> factory, Properties props) {
			return this.registerBlock(name, ignored -> factory.get(), props);
		}
	}

	/**
	 * 物品注册表
	 */
	public static class Items extends DeferredRegister<Item> {
		protected Items(Registry<Item> registry, String namespace) {
			super(registry, namespace);
		}

		public static Items createItems(String namespace) {
			return new Items(registryOf(Registries.ITEM), namespace);
		}

		public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> factory, Item.Properties props) {
			return this.registerItemValue(name, factory.apply(props));
		}

		/** 物品注册的收口入口：写入注册表并留下 DeferredItem 句柄 */
		protected <I extends Item> DeferredItem<I> registerItemValue(String name, I value) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.namespace, name);
			Registry.register(this.registry, id, value);

			DeferredItem<I> holder = new DeferredItem<>(id, value);
			this.entries.add(holder);
			return holder;
		}

		public <I extends Item> DeferredItem<I> registerItem(String name, Supplier<? extends I> factory, Item.Properties props) {
			return this.registerItem(name, ignored -> factory.get(), props);
		}

		public <I extends Item> DeferredItem<I> registerSimpleItem(String name, Item.Properties props) {
			@SuppressWarnings("unchecked")
			I value = (I) new Item(props);
			return this.registerItemValue(name, value);
		}
	}
}

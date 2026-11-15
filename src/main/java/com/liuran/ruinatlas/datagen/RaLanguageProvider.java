package com.liuran.ruinatlas.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 语言文件基类：只负责把 NeoForge 版 LanguageProvider 的 add 重载搬到 Fabric 的 TranslationBuilder 上
 * <p>两个语种的提供器正文逐字沿用 NeoForge 版，改动只在这层桥接。
 */
public abstract class RaLanguageProvider extends FabricLanguageProvider {
	private TranslationBuilder builder;

	protected RaLanguageProvider(FabricDataOutput output, String locale, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, locale, registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		this.builder = builder;
		this.addTranslations();
	}

	protected abstract void addTranslations();

	protected void add(String key, String value) {
		this.builder.add(key, value);
	}

	protected void add(Block block, String value) {
		this.builder.add(block, value);
	}

	protected void add(Item item, String value) {
		this.builder.add(item, value);
	}
}

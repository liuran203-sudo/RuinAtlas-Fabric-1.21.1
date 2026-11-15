package com.liuran.ruinatlas.platform;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

/**
 * 注册项句柄：与 NeoForge 的 DeferredHolder 同形，字段初始化时即完成绑定
 *
 * @param <T> 注册表元素类型
 * @param <I> 具体类型
 */
public class DeferredHolder<T, I extends T> implements Supplier<I> {
	private final ResourceLocation id;
	private final I value;

	public DeferredHolder(ResourceLocation id, I value) {
		this.id = id;
		this.value = value;
	}

	@Override
	public I get() {
		return this.value;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public boolean isBound() {
		return this.value != null;
	}
}

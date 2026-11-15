package com.liuran.ruinatlas.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * 物品注册项句柄
 */
public class DeferredItem<I extends Item> extends DeferredHolder<Item, I> {
	public DeferredItem(ResourceLocation id, I value) {
		super(id, value);
	}
}

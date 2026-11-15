package com.liuran.ruinatlas.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * 方块注册项句柄
 */
public class DeferredBlock<B extends Block> extends DeferredHolder<Block, B> {
	public DeferredBlock(ResourceLocation id, B value) {
		super(id, value);
	}
}

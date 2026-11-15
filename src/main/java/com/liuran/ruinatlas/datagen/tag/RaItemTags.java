package com.liuran.ruinatlas.datagen.tag;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;

import com.liuran.ruinatlas.init.RaItems;

/**
 * 物品标签生成器：让古木与古墟木板进入原版木料物品标签
 */
public class RaItemTags extends FabricTagProvider.ItemTagProvider {
	public RaItemTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries,
	                  FabricTagProvider.BlockTagProvider blockTags) {
		super(output, registries, blockTags);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		getOrCreateTagBuilder(ItemTags.LOGS).add(RaItems.CANGYIN_LOG.get(), RaItems.CANGYIN_BARK.get());
		getOrCreateTagBuilder(ItemTags.LOGS_THAT_BURN).add(RaItems.CANGYIN_LOG.get(), RaItems.CANGYIN_BARK.get());
		getOrCreateTagBuilder(ItemTags.PLANKS).add(RaItems.CANGYIN_PLANKS.get());
	}
}

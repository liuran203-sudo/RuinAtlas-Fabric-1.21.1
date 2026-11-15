package com.liuran.ruinatlas.init;

import com.liuran.ruinatlas.platform.DeferredHolder;
import com.liuran.ruinatlas.platform.DeferredRegister;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

public class RaCreativeModeTabs {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	// Fabric 的创造栏 API 没有 NeoForge 的 withTabsBefore 排序钩子，新栏位统一排在原版各项之后
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RUIN_ATLAS_TAB = CREATIVE_MODE_TABS.register("ruin_atlas", () -> FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.ruinatlas"))
			.icon(() -> RaItems.SPIRIT_HOLDING_STONE.get().getDefaultInstance())
			.displayItems((parameters, output) -> RaItems.ITEM_REGISTER.getEntries().forEach(entry -> output.accept(entry.get())))
			.build());

	public static void register() {
		CREATIVE_MODE_TABS.register();
	}
}

package com.liuran.ruinatlas.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.liuran.ruinatlas.init.RaItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

/**
 * 物品模型生成器：方块物品的模型由 RaBlockModel 一并生成，这里只处理普通物品
 */
public class RaItemModel implements DataProvider {
	private final FabricDataOutput output;

	public RaItemModel(FabricDataOutput output) {
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		Path root = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);

		for (var entry : RaItems.ITEM_REGISTER.getEntries()) {
			Item item = entry.get();

			if (item instanceof BlockItem) {
				continue;
			}

			String path = entry.getId().getPath();
			JsonObject model = new JsonObject();
			model.addProperty("parent", "minecraft:item/generated");
			JsonObject textures = new JsonObject();
			textures.addProperty("layer0", MODID + ":item/" + path);
			model.add("textures", textures);

			futures.add(DataProvider.saveStable(cache, model, root.resolve(MODID + "/models/item/" + path + ".json")));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "RaItemModel";
	}
}

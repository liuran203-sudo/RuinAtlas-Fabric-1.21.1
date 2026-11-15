package com.liuran.ruinatlas.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.liuran.ruinatlas.block.RotatableRuinBlock;
import com.liuran.ruinatlas.init.RaBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

/**
 * 方块状态与方块模型生成器
 * <p>
 * 分三类处理：
 * <ol>
 *   <li>手绘自定义模型（Blockbench 导出，放在 src/main/resources/assets/ruinatlas/models/block）
 *       —— 只生成 blockstate 与 item 模型，模型本体直接引用现成文件，不再覆盖</li>
 *   <li>古木（RotatedPillarBlock）—— 走 cube_column + axis 变体 × 3</li>
 *   <li>其余 —— 统一 cube_all 占位</li>
 * </ol>
 * <p>Fabric 侧没有方块状态生成器 API，这里直接写出与 NeoForge 版同形的 JSON。
 */
public class RaBlockModel implements DataProvider {
	/** 已有手绘模型的方块，模型文件在 src/main/resources，datagen 不能覆盖 */
	private static final Set<String> CUSTOM_MODELS = Set.of(
			"ancient_book_casket",
			"baluster_solid",
			"blank_stele",
			"bone_pile",
			"bone_sacrifice_table",
			"boundary_stele",
			"bracket_shard_corner",
			"bracket_shard_side",
			"broken_baluster",
			"broken_lattice",
			"broken_pillar_lower",
			"broken_pillar_upper",
			"broken_tile_pile",
			"broken_xun_wall",
			"cangyin_gate_stele",
			"cangyin_offering_table",
			"city_curb",
			"collapsed_kiosk",
			"door_frame_ruin",
			"eave_board_shard",
			"eave_ornament",
			"empty_ceramic_urn",
			"farm_ruin_plot",
			"foundation_skirt",
			"inscribed_stele",
			"lattice_wave_window",
			"lattice_window_square",
			"lotus_plinth",
			"market_rubble",
			"newel_post",
			"pillar_brace_shard",
			"plain_plinth",
			"ridge_ornament_shard",
			"ridge_tile",
			"rusted_bronze_panel",
			"rubble_pile",
			"sealed_ceramic_urn",
			"spirit_platform",
			"spirit_test_altar",
			"stone_incense_table",
			"stone_lantern",
			"stone_plinth",
			"thatch_rubble",
			"wall_niche_small",
			"waystone",
			"wooden_pillar_lower",
			"wooden_pillar_upper",
			"wooden_screen"
	);

	/** 植被方块：走原版 cross（十字）模型，不用 cube_all */
	private static final Set<String> PLANT_BLOCKS = Set.of(
			"petal_sprout",
			"petal_thicket",
			"wither_reed",
			"xun_lily",
			"xun_vine",
			"xun_wild_grass"
	);

	private final FabricDataOutput output;

	public RaBlockModel(FabricDataOutput output) {
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<CompletableFuture<?>> futures = new ArrayList<>();

		for (var entry : RaBlocks.BLOCK_REGISTER.getEntries()) {
			Block block = entry.get();
			String path = entry.getId().getPath();

			if (block == RaBlocks.CANGYIN_LOG.get()) {
				// 古木单独处理，见下方
				continue;
			}

			// 台阶：继承原版台阶（SlabBlock），走原版 type=bottom/top/double 变体，
			// 下半与上半各一张 slab 模型，双台阶用整块模型
			if (block instanceof SlabBlock) {
				futures.add(save(cache, slabBlockState(path), "blockstates/" + path + ".json"));
				futures.add(save(cache, slabModel("minecraft:block/slab", path), "models/block/" + path + "_slab.json"));
				futures.add(save(cache, slabModel("minecraft:block/slab_top", path), "models/block/" + path + "_slab_top.json"));
				futures.add(save(cache, cubeAllModel(path), "models/block/" + path + ".json"));
				futures.add(save(cache, itemModel(MODID + ":block/" + path + "_slab"), "models/item/" + path + ".json"));
				continue;
			}

			// 有朝向的构件（案、桌、屏风、铜牌、栏杆、花窗、檐板、门框、墙龛、碑刻）
			// 一律出四向变体，是否转向由方块类自己决定，不再维护名字白名单
			if (block instanceof RotatableRuinBlock) {
				futures.add(save(cache, facingBlockState(path), "blockstates/" + path + ".json"));
				futures.add(save(cache, blockItemModel(path), "models/item/" + path + ".json"));
				continue;
			}

			if (CUSTOM_MODELS.contains(path)) {
				futures.add(save(cache, singleVariantBlockState(path), "blockstates/" + path + ".json"));
				futures.add(save(cache, blockItemModel(path), "models/item/" + path + ".json"));
				continue;
			}

			if (PLANT_BLOCKS.contains(path)) {
				// 植被走原版 cross 模型：十字交叉片 + cutout，物品栏用平面贴图
				futures.add(save(cache, singleVariantBlockState(path), "blockstates/" + path + ".json"));
				futures.add(save(cache, crossModel(path), "models/block/" + path + ".json"));
				futures.add(save(cache, generatedItemModel(MODID + ":block/" + path), "models/item/" + path + ".json"));
				continue;
			}

			futures.add(save(cache, singleVariantBlockState(path), "blockstates/" + path + ".json"));
			futures.add(save(cache, cubeAllModel(path), "models/block/" + path + ".json"));
			futures.add(save(cache, blockItemModel(path), "models/item/" + path + ".json"));
		}

		RotatedPillarBlock log = RaBlocks.CANGYIN_LOG.get();
		JsonObject logVariants = new JsonObject();
		logVariants.add("axis=x", rotation(MODID + ":block/cangyin_log_horizontal", 90, 90));
		logVariants.add("axis=y", noRotation(MODID + ":block/cangyin_log"));
		logVariants.add("axis=z", rotation(MODID + ":block/cangyin_log_horizontal", 90, 0));
		futures.add(save(cache, blockState(logVariants), "blockstates/cangyin_log.json"));
		futures.add(save(cache, cubeColumnModel("cangyin_log"), "models/block/cangyin_log.json"));
		futures.add(save(cache, cubeColumnHorizontalModel("cangyin_log_horizontal"), "models/block/cangyin_log_horizontal.json"));
		futures.add(save(cache, blockItemModel("cangyin_log"), "models/item/cangyin_log.json"));

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "RaBlockModel";
	}

	private CompletableFuture<?> save(CachedOutput cache, JsonElement json, String path) {
		Path root = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
		return DataProvider.saveStable(cache, json, root.resolve(MODID + "/" + path));
	}

	// ===== JSON 组装 =====

	private static JsonObject blockState(JsonObject variants) {
		JsonObject root = new JsonObject();
		root.add("variants", variants);
		return root;
	}

	private static JsonObject singleVariantBlockState(String path) {
		JsonObject variants = new JsonObject();
		variants.add("", noRotation(MODID + ":block/" + path));
		return blockState(variants);
	}

	private static JsonObject facingBlockState(String path) {
		String model = MODID + ":block/" + path;
		JsonObject variants = new JsonObject();
		variants.add("facing=north", noRotation(model));
		variants.add("facing=east", rotation(model, 0, 90));
		variants.add("facing=south", rotation(model, 0, 180));
		variants.add("facing=west", rotation(model, 0, 270));
		return blockState(variants);
	}

	private static JsonObject noRotation(String model) {
		JsonObject variant = new JsonObject();
		variant.addProperty("model", model);
		return variant;
	}

	private static JsonObject rotation(String model, int x, int y) {
		JsonObject variant = new JsonObject();
		variant.addProperty("model", model);

		if (x != 0) {
			variant.addProperty("x", x);
		}

		if (y != 0) {
			variant.addProperty("y", y);
		}

		return variant;
	}

	private static JsonObject blockItemModel(String path) {
		return itemModel(MODID + ":block/" + path);
	}

	private static JsonObject itemModel(String parent) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", parent);
		return model;
	}

	/** 台阶三变体：下半、上半、双台阶（整块模型） */
	private static JsonObject slabBlockState(String path) {
		String model = MODID + ":block/" + path;
		JsonObject variants = new JsonObject();
		variants.add("type=bottom", noRotation(model + "_slab"));
		variants.add("type=top", noRotation(model + "_slab_top"));
		variants.add("type=double", noRotation(model));
		return blockState(variants);
	}

	/** 台阶半砖模型：上下与侧面都用整块那张贴图 */
	private static JsonObject slabModel(String parent, String path) {
		String texture = MODID + ":block/" + path;
		JsonObject model = new JsonObject();
		model.addProperty("parent", parent);
		JsonObject textures = new JsonObject();
		textures.addProperty("bottom", texture);
		textures.addProperty("top", texture);
		textures.addProperty("side", texture);
		model.add("textures", textures);
		return model;
	}

	private static JsonObject generatedItemModel(String texture) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:item/generated");
		JsonObject textures = new JsonObject();
		textures.addProperty("layer0", texture);
		model.add("textures", textures);
		return model;
	}

	private static JsonObject cubeAllModel(String path) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/cube_all");
		JsonObject textures = new JsonObject();
		textures.addProperty("all", MODID + ":block/" + path);
		model.add("textures", textures);
		return model;
	}

	private static JsonObject crossModel(String path) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/cross");
		model.addProperty("render_type", "minecraft:cutout");
		JsonObject textures = new JsonObject();
		textures.addProperty("cross", MODID + ":block/" + path);
		model.add("textures", textures);
		return model;
	}

	private static JsonObject cubeColumnModel(String path) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/cube_column");
		JsonObject textures = new JsonObject();
		textures.addProperty("end", MODID + ":block/cangyin_log_top");
		textures.addProperty("side", MODID + ":block/cangyin_log_side");
		model.add("textures", textures);
		return model;
	}

	private static JsonObject cubeColumnHorizontalModel(String path) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/cube_column_horizontal");
		JsonObject textures = new JsonObject();
		textures.addProperty("end", MODID + ":block/cangyin_log_top");
		textures.addProperty("side", MODID + ":block/cangyin_log_side");
		model.add("textures", textures);
		return model;
	}
}

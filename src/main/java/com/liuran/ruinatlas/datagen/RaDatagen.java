package com.liuran.ruinatlas.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.DetectedVersion;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

import com.liuran.ruinatlas.datagen.loot.RaLootTableProvider;
import com.liuran.ruinatlas.datagen.recipe.RaRecipeProvider;
import com.liuran.ruinatlas.datagen.tag.RaBlockTags;
import com.liuran.ruinatlas.datagen.tag.RaItemTags;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

/**
 * 数据生成主类：runData 时挂载全部 Provider
 * <p>Fabric 侧一次性进同一个 Pack，不再区分 client / server 开关；
 * 举凡语言文件、标签、战利品表、配方都从这里登记。
 */
public class RaDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		// 包元数据：写明资源包描述与数据包版本
		net.minecraft.data.DataProvider.Factory<PackMetadataGenerator> metadata = output -> new PackMetadataGenerator(output)
				.add(PackMetadataSection.TYPE, new PackMetadataSection(
						Component.translatable("pack." + MODID + ".description"),
						DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
						java.util.Optional.empty()));
		pack.addProvider(metadata);

		// 客户端数据生成
		pack.addProvider(RaBlockModel::new);
		pack.addProvider(RaItemModel::new);
		pack.addProvider(RaI18ZhCn::new);
		pack.addProvider(RaI18EnUs::new);

		// 服务端数据生成
		RaBlockTags blockTags = pack.addProvider(RaBlockTags::new);
		pack.addProvider((output, registries) -> new RaItemTags(output, registries, blockTags));
		pack.addProvider(RaLootTableProvider::new);
		pack.addProvider(RaRecipeProvider::new);
	}
}

package com.liuran.ruinatlas;

import com.liuran.ruinatlas.event.RaSpiritEvents;
import com.liuran.ruinatlas.init.RaAttachments;
import com.liuran.ruinatlas.init.RaBlockEntities;
import com.liuran.ruinatlas.init.RaBlocks;
import com.liuran.ruinatlas.init.RaCreativeModeTabs;
import com.liuran.ruinatlas.init.RaItems;
import com.liuran.ruinatlas.init.RaParticleTypes;
import com.liuran.ruinatlas.network.RaNetwork;
import com.liuran.ruinatlas.technique.RaTechniqueRegistry;
import com.liuran.ruinatlas.worldgen.ModWorldgen;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class RuinAtlas implements ModInitializer {
	// Define mod id in a common place for everything to reference
	public static final String MODID = "ruinatlas";
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();

	// Fabric 的入口回调即 NeoForge 版中 mod 类构造器的位置
	@Override
	public void onInitialize() {
		// 读取本地配置
		Config.load();

		// 墟舆录 init 装配：每个类别一个 DeferredRegister，类内注册到注册表
		RaBlocks.register();
		RaBlockEntities.register();
		RaItems.register();
		RaParticleTypes.register();
		RaAttachments.register();
		RaCreativeModeTabs.register();
		ModWorldgen.register();

		// 网络包注册
		RaNetwork.register();

		// 游戏内事件挂载
		RaSpiritEvents.register();
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);

		// Register the commonSetup
		this.commonSetup();
	}

	private void commonSetup() {
		RaTechniqueRegistry.bootstrap();
		LOGGER.info("墟舆录 Ruin Atlas 初始化完成");
	}

	public void onServerStarting(MinecraftServer server) {
		LOGGER.info("墟舆录维度加载中");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}

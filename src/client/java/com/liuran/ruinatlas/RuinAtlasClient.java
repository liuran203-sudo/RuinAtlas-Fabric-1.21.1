package com.liuran.ruinatlas;

import net.fabricmc.api.ClientModInitializer;

import com.liuran.ruinatlas.client.RaClientEvents;
import com.liuran.ruinatlas.client.RaClientSetup;
import com.liuran.ruinatlas.client.SpiritAuraClient;

/**
 * 客户端入口：按键、灵息 HUD、粒子、环境灵息
 * <p>只在客户端装载，服务端不会碰到这里的类。
 */
public class RuinAtlasClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 粒子渲染提供器
		com.liuran.ruinatlas.client.RuinAtlasClient.register();
		// 按键 + 灵息 HUD
		RaClientSetup.register();
		// 按键的逐刻处理
		RaClientEvents.register();
		// 苍垠古墟界的环境灵息
		SpiritAuraClient.register();
	}
}

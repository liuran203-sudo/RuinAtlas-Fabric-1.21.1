package com.liuran.ruinatlas.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import com.liuran.ruinatlas.network.CastTechniquePayload;

/**
 * 客户端按键处理：R 施法，G 打开灵契轮盘（占位界面）
 */
public final class RaClientEvents {
	private RaClientEvents() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(RaClientEvents::onClientTick);
	}

	private static void onClientTick(Minecraft minecraft) {
		if (minecraft.player == null) {
			return;
		}

		while (RaKeyMappings.CAST.consumeClick()) {
			if (minecraft.screen == null) {
				ClientPlayNetworking.send(new CastTechniquePayload(""));
			}
		}

		if (RaKeyMappings.WHEEL.consumeClick() && minecraft.screen == null) {
			minecraft.setScreen(new RaTechniqueScreen());
		}
	}
}

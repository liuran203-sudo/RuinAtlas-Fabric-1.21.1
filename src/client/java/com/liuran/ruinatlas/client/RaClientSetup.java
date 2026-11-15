package com.liuran.ruinatlas.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * 客户端注册：按键 + 灵息 HUD
 */
public final class RaClientSetup {
	private RaClientSetup() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(RaKeyMappings.CAST);
		KeyBindingHelper.registerKeyBinding(RaKeyMappings.WHEEL);

		HudRenderCallback.EVENT.register(RaSpiritHud::render);
	}
}

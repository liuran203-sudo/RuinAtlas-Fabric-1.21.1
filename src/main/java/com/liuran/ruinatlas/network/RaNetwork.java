package com.liuran.ruinatlas.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

/**
 * 本模组的网络包：只走客户端 → 服务端
 * <p>Fabric 侧分两步：先在 PayloadTypeRegistry 登记包类型与编解码器，再挂接收器。
 */
public final class RaNetwork {
	private RaNetwork() {
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(CastTechniquePayload.TYPE, CastTechniquePayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SelectTechniquePayload.TYPE, SelectTechniquePayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(CastTechniquePayload.TYPE,
				(payload, context) -> context.server().execute(() -> CastTechniquePayload.handle(payload, context.player())));
		ServerPlayNetworking.registerGlobalReceiver(SelectTechniquePayload.TYPE,
				(payload, context) -> context.server().execute(() -> SelectTechniquePayload.handle(payload, context.player())));
	}
}

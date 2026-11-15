package com.liuran.ruinatlas.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

import com.liuran.ruinatlas.init.RaParticleTypes;

/**
 * 客户端注册：粒子渲染提供器
 */
public final class RuinAtlasClient {
	private RuinAtlasClient() {
	}

	public static void register() {
		ParticleFactoryRegistry.getInstance().register(
				RaParticleTypes.SPIRIT_MIST.get(),
				sprites -> (type, level, x, y, z, dx, dy, dz) -> new SpiritMistParticle(level, x, y, z, sprites)
		);
		ParticleFactoryRegistry.getInstance().register(
				RaParticleTypes.SPIRIT_GLOW.get(),
				sprites -> (type, level, x, y, z, dx, dy, dz) -> new SpiritGlowParticle(level, x, y, z, sprites)
		);
	}
}

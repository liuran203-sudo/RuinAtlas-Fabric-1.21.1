package com.liuran.ruinatlas.init;

import java.util.function.Supplier;

import com.liuran.ruinatlas.platform.DeferredRegister;
import com.liuran.ruinatlas.platform.RaSimpleParticleType;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

public class RaParticleTypes {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
			DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);

	// 灵霭：残碑、涧谷、灵脉上方缓慢漂移的淡青白雾絮
	public static final Supplier<SimpleParticleType> SPIRIT_MIST = PARTICLE_TYPES.register("spirit_mist", () -> new RaSimpleParticleType(false));
	// 灵息微光：蚀纹岩、玉屑岩露头处的低频脉动青光
	public static final Supplier<SimpleParticleType> SPIRIT_GLOW = PARTICLE_TYPES.register("spirit_glow", () -> new RaSimpleParticleType(false));

	public static void register() {
		PARTICLE_TYPES.register();
	}
}

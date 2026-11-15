package com.liuran.ruinatlas.platform;

import net.minecraft.core.particles.SimpleParticleType;

/**
 * 可公开构造的 SimpleParticleType
 * <p>Fabric 不修改原版可见性，原版 SimpleParticleType 的构造器是 protected；
 * 这里用同包能力开放出来，注册与序列化行为与原版完全一致，供 {@code RaParticleTypes} 使用。
 */
public class RaSimpleParticleType extends SimpleParticleType {
	public RaSimpleParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}
}

package com.liuran.ruinatlas.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import com.liuran.ruinatlas.init.RaAttachments;
import com.liuran.ruinatlas.technique.RaTechniqueRegistry;

/**
 * 灵息相关的逐刻结算：引息抹锁定、踏岳免摔、护息替挡一次伤害
 * <p>Fabric 侧的对应挂载点：服务端 tick 结尾遍历玩家 + 生物受伤前的允许判定
 */
public final class RaSpiritEvents {
	private RaSpiritEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(RaSpiritEvents::onServerTick);
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(RaSpiritEvents::allowDamage);
	}

	private static void onServerTick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			onPlayerTick(player);
		}
	}

	private static void onPlayerTick(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		long silencedUntil = RaAttachments.getSilencedUntil(player);

		if (silencedUntil > 0L) {
			if (level.getGameTime() >= silencedUntil) {
				RaAttachments.setSilencedUntil(player, 0L);
			} else if (player.tickCount % 4 == 0) {
				RaTechniqueRegistry.silenceNearby(level, player);
			}
		}

		long fallGuardUntil = RaAttachments.getFallGuardUntil(player);

		if (fallGuardUntil > 0L) {
			if (level.getGameTime() >= fallGuardUntil) {
				RaAttachments.setFallGuardUntil(player, 0L);
			} else {
				player.resetFallDistance();
			}
		}
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
			return true;
		}

		long wardUntil = RaAttachments.getWardUntil(player);

		if (wardUntil <= 0L || level.getGameTime() >= wardUntil) {
			return true;
		}

		RaAttachments.setWardUntil(player, 0L);

		level.sendParticles(
				com.liuran.ruinatlas.init.RaParticleTypes.SPIRIT_MIST.get(),
				player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
				18, 0.4, 0.4, 0.4, 0.01);
		return false;
	}
}

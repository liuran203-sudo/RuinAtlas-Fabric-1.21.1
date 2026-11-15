package com.liuran.ruinatlas.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static com.liuran.ruinatlas.RuinAtlas.MODID;

/**
 * 玩家身上的灵息与契痕
 * <p>SPIRIT 与 LEARNED / RANKS / SELECTED 都做了 sync，客户端才能画轮盘与 HUD。
 * 灵息不随死（本来就不是修为），契痕随死保留（已经烙进脉络）。
 * <p>Fabric 侧用官方的 data attachment 接口承载这些玩家数据；NeoForge 版对应 AttachmentType。
 */
public final class RaAttachments {
	public static final Codec<List<ResourceLocation>> LEARNED_CODEC = ResourceLocation.CODEC.listOf();
	public static final Codec<Map<String, Integer>> RANKS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
	public static final Codec<Map<String, Long>> COOLDOWN_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);

	public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, List<ResourceLocation>> LEARNED_STREAM =
			ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC);
	public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, Map<String, Integer>> RANKS_STREAM =
			ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);

	/** 灵息余量 */
	public static final AttachmentType<Integer> SPIRIT = AttachmentRegistry.create(
			key("spirit"), builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT)
					.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()));

	/** 已契悟的契法 id */
	public static final AttachmentType<List<ResourceLocation>> LEARNED = AttachmentRegistry.create(
			key("learned"), builder -> builder
					.initializer(List::of)
					.persistent(LEARNED_CODEC)
					.syncWith(LEARNED_STREAM, AttachmentSyncPredicate.targetOnly())
					.copyOnDeath());

	/** 各契法的契痕层数（1 初契 / 2 道凝 / 3 契满） */
	public static final AttachmentType<Map<String, Integer>> RANKS = AttachmentRegistry.create(
			key("technique_ranks"), builder -> builder
					.initializer(Map::of)
					.persistent(RANKS_CODEC)
					.syncWith(RANKS_STREAM, AttachmentSyncPredicate.targetOnly())
					.copyOnDeath());

	/** 当前所指的契法（存 path） */
	public static final AttachmentType<String> SELECTED = AttachmentRegistry.create(
			key("selected"), builder -> builder
					.initializer(() -> "")
					.persistent(Codec.STRING)
					.syncWith(ByteBufCodecs.STRING_UTF8, AttachmentSyncPredicate.targetOnly())
					.copyOnDeath());

	/** 各契法的冷却到期刻（不落盘、不随死） */
	public static final AttachmentType<Map<String, Long>> COOLDOWNS = AttachmentRegistry.create(
			key("technique_cooldowns"), builder -> builder
					.initializer(Map::of)
					.persistent(COOLDOWN_CODEC));

	/** 引息静默到期时刻（游戏刻） */
	public static final AttachmentType<Long> SILENCED_UNTIL = AttachmentRegistry.create(
			key("silenced_until"), builder -> builder
					.initializer(() -> 0L)
					.persistent(Codec.LONG));

	/** 踏岳免摔到期时刻（游戏刻） */
	public static final AttachmentType<Long> FALL_GUARD_UNTIL = AttachmentRegistry.create(
			key("fall_guard_until"), builder -> builder
					.initializer(() -> 0L)
					.persistent(Codec.LONG));

	/** 护息挡伤到期时刻（游戏刻），挡下一次伤害后立即归零 */
	public static final AttachmentType<Long> WARD_UNTIL = AttachmentRegistry.create(
			key("ward_until"), builder -> builder
					.initializer(() -> 0L)
					.persistent(Codec.LONG));

	/** 墟龛代付的免灵息施术次数 */
	public static final AttachmentType<Integer> FREE_CASTS = AttachmentRegistry.create(
			key("free_casts"), builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT));

	private RaAttachments() {
	}

	private static ResourceLocation key(String name) {
		return ResourceLocation.fromNamespaceAndPath(MODID, name);
	}

	public static void register() {
		// 数据附加在字段初始化阶段已登记到 AttachmentRegistry，无需额外动作
	}

	// ===== 灵息 =====

	public static int getSpirit(Player player) {
		return player.getAttachedOrCreate(SPIRIT);
	}

	public static void setSpirit(Player player, int value) {
		player.setAttached(SPIRIT, Math.max(0, value));
	}

	public static void addSpirit(Player player, int amount) {
		setSpirit(player, getSpirit(player) + amount);
	}

	public static boolean spendSpirit(Player player, int amount) {
		if (getSpirit(player) < amount) {
			return false;
		}

		setSpirit(player, getSpirit(player) - amount);
		return true;
	}

	// ===== 契痕 =====

	public static List<ResourceLocation> getLearned(Player player) {
		return player.getAttachedOrCreate(LEARNED);
	}

	public static boolean hasLearned(Player player, ResourceLocation id) {
		return getLearned(player).contains(id);
	}

	public static void learn(Player player, ResourceLocation id) {
		List<ResourceLocation> learned = new ArrayList<>(getLearned(player));

		if (!learned.contains(id)) {
			learned.add(id);
			player.setAttached(LEARNED, List.copyOf(learned));
			setRank(player, id, 1);
		}
	}

	public static int getRank(Player player, ResourceLocation id) {
		return player.getAttachedOrCreate(RANKS).getOrDefault(id.getPath(), 1);
	}

	public static void setRank(Player player, ResourceLocation id, int rank) {
		Map<String, Integer> ranks = new HashMap<>(player.getAttachedOrCreate(RANKS));
		ranks.put(id.getPath(), Math.min(3, Math.max(1, rank)));
		player.setAttached(RANKS, Map.copyOf(ranks));
	}

	/** 加深一层契痕，返回新层数（已满则不升） */
	public static int raiseRank(Player player, ResourceLocation id) {
		int current = getRank(player, id);

		if (current >= 3) {
			return 3;
		}

		setRank(player, id, current + 1);
		return current + 1;
	}

	public static String getSelected(Player player) {
		return player.getAttachedOrCreate(SELECTED);
	}

	public static void setSelected(Player player, String path) {
		player.setAttached(SELECTED, path);
	}

	// ===== 冷却 =====

	public static long getCooldownReady(Player player, String path) {
		return player.getAttachedOrCreate(COOLDOWNS).getOrDefault(path, 0L);
	}

	public static void setCooldownReady(Player player, String path, long tick) {
		Map<String, Long> map = new HashMap<>(player.getAttachedOrCreate(COOLDOWNS));

		if (tick <= 0L) {
			map.remove(path);
		} else {
			map.put(path, tick);
		}

		player.setAttached(COOLDOWNS, Map.copyOf(map));
	}

	// ===== 状态计时 =====

	public static long getSilencedUntil(Player player) {
		return player.getAttachedOrCreate(SILENCED_UNTIL);
	}

	public static void setSilencedUntil(Player player, long tick) {
		player.setAttached(SILENCED_UNTIL, tick);
	}

	public static long getFallGuardUntil(Player player) {
		return player.getAttachedOrCreate(FALL_GUARD_UNTIL);
	}

	public static void setFallGuardUntil(Player player, long tick) {
		player.setAttached(FALL_GUARD_UNTIL, tick);
	}

	public static long getWardUntil(Player player) {
		return player.getAttachedOrCreate(WARD_UNTIL);
	}

	public static void setWardUntil(Player player, long tick) {
		player.setAttached(WARD_UNTIL, tick);
	}

	public static int getFreeCasts(Player player) {
		return player.getAttachedOrCreate(FREE_CASTS);
	}

	public static void setFreeCasts(Player player, int casts) {
		player.setAttached(FREE_CASTS, Math.max(0, casts));
	}
}

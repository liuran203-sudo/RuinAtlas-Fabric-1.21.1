package com.liuran.ruinatlas.mixin;

import com.liuran.ruinatlas.worldgen.RuinDimensions;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 苍垠昼时写穿：非主世界维度的 DerivedLevelData 里 setDayTime 是空实现，
 * 睡觉跳夜、/time set 都会被丢掉；各维度共用一份昼时，这里改写到主世界。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelDayTimeMixin {
	@Inject(method = "setDayTime", at = @At("HEAD"))
	private void ruinatlas$shareDayTime(long time, CallbackInfo ci) {
		ServerLevel level = (ServerLevel) (Object) this;

		if (level.getServer() == null || !RuinDimensions.CANGYIN.equals(level.dimension())) {
			return;
		}

		level.getServer().overworld().setDayTime(time);
	}
}

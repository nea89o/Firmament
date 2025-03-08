package moe.nea.firmament.mixins.compat.jade;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.compat.jade.CustomMiningHardnessProvider;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import snownee.jade.JadeClient;

@Mixin(JadeClient.class)
public class PatchBreakingBarSpeedJade {
	@ModifyExpressionValue(
		method = "drawBreakingProgress",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;currentBreakingProgress:F", opcode = Opcodes.GETFIELD)
	)
	private static float replaceBlockBreakingProgress(float original) {
		return CustomMiningHardnessProvider.replaceBreakProgress(original);
	}

	@ModifyExpressionValue(method = "drawBreakingProgress",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
	private static float replacePlayerSpecificBreakingProgress(float original) {
		return CustomMiningHardnessProvider.replaceBlockBreakSpeed(original);
	}
}

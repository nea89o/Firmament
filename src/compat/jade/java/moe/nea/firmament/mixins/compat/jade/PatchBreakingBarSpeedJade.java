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
	// TODO: given the inherent roughness of the server provided stages, i don't feel the need to also patch the accesses to the delta provided by the block state. if i ever get around to adding the linear extrapolation, i should also patch that extrapolation to use the better one provided by the server stages.
}

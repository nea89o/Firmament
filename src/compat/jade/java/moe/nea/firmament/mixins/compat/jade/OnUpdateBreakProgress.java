package moe.nea.firmament.mixins.compat.jade;

import moe.nea.firmament.compat.jade.CustomMiningHardnessProvider;
import moe.nea.firmament.util.MC;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(WorldRenderer.class)
public class OnUpdateBreakProgress {
	@Inject(method = "setBlockBreakingInfo", at = @At("HEAD"))
	private void replaceBreakProgress(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
		if (entityId == 0 && null != MC.INSTANCE.getInteractionManager() && Objects.equals(MC.INSTANCE.getInteractionManager().currentBreakingPos, pos)) {
			CustomMiningHardnessProvider.setBreakingInfo(pos, stage);
		}
	}
}

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class DisableHurtCam {
	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;hurtTime:F", opcode = Opcodes.GETFIELD))
	private float replaceHurtTime(float original) {
		if (Fixes.TConfig.INSTANCE.getNoHurtCam())
			return 0F;
		return original;
	}
}

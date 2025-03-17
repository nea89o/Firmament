package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.render.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class DisableHurtCam {
	@ModifyExpressionValue(method = "tiltViewWhenHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;hurtTime:I", opcode = Opcodes.GETFIELD))
	private int replaceHurtTime(int original) {
		if (Fixes.TConfig.INSTANCE.getNoHurtCam())
			return 0;
		return original;
	}
}

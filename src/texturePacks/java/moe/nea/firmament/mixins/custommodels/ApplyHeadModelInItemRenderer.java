
package moe.nea.firmament.mixins.custommodels;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class ApplyHeadModelInItemRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
	// TODO: replace head_model with a condition model (if possible, automatically)
	// TODO: ItemAsset.CODEC should upgrade partials
	@Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL"))
	private void updateHeadState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {

	}
}

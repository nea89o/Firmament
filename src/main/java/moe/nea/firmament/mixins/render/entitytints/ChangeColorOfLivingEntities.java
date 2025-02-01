package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies various rendering modifications from {@link EntityRenderTintEvent}
 */
@Mixin(LivingEntityRenderer.class)
public class ChangeColorOfLivingEntities<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
	@ModifyReturnValue(method = "getMixColor", at = @At("RETURN"))
	private int changeColor(int original, @Local(argsOnly = true) S state) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		if (tintState.getHasTintOverride_firmament())
			return tintState.getTint_firmament();
		return original;
	}

	@ModifyArg(
		method = "getOverlay",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OverlayTexture;getU(F)I"),
		allow = 1
	)
	private static float modifyLightOverlay(float originalWhiteOffset, @Local(argsOnly = true) LivingEntityRenderState state) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		if (tintState.getHasTintOverride_firmament() || tintState.getOverlayTexture_firmament() != null) {
			return 1F; // TODO: add interpolation percentage to render state extension
		}
		return originalWhiteOffset;
	}

	@Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
	private void afterRender(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(livingEntityRenderState);
		var overlayTexture = tintState.getOverlayTexture_firmament();
		if (overlayTexture != null && vertexConsumerProvider instanceof VertexConsumerProvider.Immediate imm) {
			imm.drawCurrentLayer();
		}
		EntityRenderTintEvent.overlayOverride = null;
	}

	@Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
	private void beforeRender(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(livingEntityRenderState);
		var overlayTexture = tintState.getOverlayTexture_firmament();
		if (overlayTexture != null) {
			EntityRenderTintEvent.overlayOverride = overlayTexture;
		}
	}
}

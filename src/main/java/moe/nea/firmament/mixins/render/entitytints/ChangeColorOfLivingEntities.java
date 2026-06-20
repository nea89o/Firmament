package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
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
	@ModifyReturnValue(method = "getModelTint", at = @At("RETURN"))
	private int changeColor(int original, @Local(argsOnly = true) S state) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		if (tintState.getHasTintOverride_firmament())
			return tintState.getTint_firmament();
		return original;
	}

	@ModifyArg(
		method = "getOverlayCoords",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/OverlayTexture;u(F)I"),
		allow = 1
	)
	private static float modifyLightOverlay(float originalWhiteOffset, @Local(argsOnly = true) LivingEntityRenderState state) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		if (tintState.getHasTintOverride_firmament() || tintState.getOverlayTexture_firmament() != null) {
			return 1F; // TODO: add interpolation percentage to render state extension
		}
		return originalWhiteOffset;
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void afterRender(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
//		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(livingEntityRenderState);
//		var overlayTexture = tintState.getOverlayTexture_firmament();
//		if (overlayTexture != null && vertexConsumerProvider instanceof VertexConsumerProvider.Immediate imm) {
//			imm.drawCurrentLayer();
//		}
//		EntityRenderTintEvent.overlayOverride = null;
		// TODO: 1.21.10
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
	private void beforeRender(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		var tintState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		var overlayTexture = tintState.getOverlayTexture_firmament();
		if (overlayTexture != null) {
			EntityRenderTintEvent.overlayOverride = overlayTexture;
		}
	}
}

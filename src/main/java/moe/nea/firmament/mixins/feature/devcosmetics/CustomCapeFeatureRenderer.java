package moe.nea.firmament.mixins.feature.devcosmetics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.Unit;
import moe.nea.firmament.features.misc.CustomCapes;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public abstract class CustomCapeFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public CustomCapeFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> context) {
		super(context);
	}

//	@WrapOperation(
//		method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
//		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
//	)
//	private void onRender(SubmitNodeCollector instance, Model model, Object o, PoseStack matrixStack, RenderType renderLayer, int light, int overlay, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand, Operation<Void> original,
//                          @Local(argsOnly = true) AvatarRenderState playerEntityRenderState, @Local PlayerSkin skinTextures) {
//		// TODO: 1.21.10 custom capes by pre rendering the texture id. this is more viable on this version i am fairly sure, without clogging up all of the cached image render layers
//		CustomCapes.render(
//			playerEntityRenderState,
//			vertexConsumer,
//			RenderLayer.getEntitySolid(skinTextures.cape().id()),
//			vertexConsumerProvider,
//			matrixStack,
//			updatedConsumer -> {
//				original.call(instance, matrixStack, updatedConsumer, light, overlay, outlineColor);
//				return Unit.INSTANCE;
//			});
//	}
}

package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patch to make {@link HeadFeatureRenderer} use a {@link RenderLayer} that allows uses Minecraft's overlay texture, if a {@link EntityRenderTintEvent#overlayOverride} is specified.
 * @see UseOverlayableItemRenderer
 */
@Mixin(HeadFeatureRenderer.class)
public class UseOverlayableHeadFeatureRenderer {

	@ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/OverlayTexture;DEFAULT_UV:I"))
	private int replaceUvIndex(int original) {
		if (EntityRenderTintEvent.overlayOverride != null)
			return OverlayTexture.packUv(15, 10); // TODO: store this info in a global alongside overlayOverride
		return original;
	}
}

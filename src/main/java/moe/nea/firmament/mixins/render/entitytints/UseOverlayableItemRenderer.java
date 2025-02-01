package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patch to make {@link ItemRenderState} use a {@link RenderLayer} that allows uses Minecraft's overlay texture.
 *
 * @see UseOverlayableHeadFeatureRenderer
 */
@Mixin(ItemRenderState.LayerRenderState.class)
public class UseOverlayableItemRenderer {
	@ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderState$LayerRenderState;renderLayer:Lnet/minecraft/client/render/RenderLayer;"))
	private RenderLayer replace(RenderLayer original) {
		if (EntityRenderTintEvent.overlayOverride != null && original instanceof RenderLayer.MultiPhase multiPhase && multiPhase.phases.texture instanceof RenderPhase.Texture texture && texture.getId().isPresent())
			return RenderLayer.getEntityTranslucent(texture.getId().get());
		return original;
	}
}

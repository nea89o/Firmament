package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.textures.GpuTextureView;

import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Replaces the overlay texture used by rendering with the override specified in {@link EntityRenderTintEvent#overlayOverride}
 */
@Mixin(RenderSetup.class)
public class ReplaceOverlayTexture {
	@ModifyVariable(method = "prepareTextures", at = @At("LOAD"), name = "overlayTexture")
	private static GpuTextureView replaceOverlayTexture(GpuTextureView original) {
		return EntityRenderTintEvent.overlayOverride != null ? EntityRenderTintEvent.overlayOverride.getTextureView() : original;
	}
}

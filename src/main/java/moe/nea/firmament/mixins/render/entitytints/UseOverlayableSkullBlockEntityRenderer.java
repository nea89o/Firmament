package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patch to make {@link SkullBlockRenderer} use a {@link net.minecraft.client.renderer.rendertype.RenderType} that allows uses Minecraft's overlay texture, if a {@link EntityRenderTintEvent#overlayOverride} is specified.
 */

@Mixin(SkullBlockRenderer.class)
public class UseOverlayableSkullBlockEntityRenderer {
	@ModifyExpressionValue(method = "submitSkull",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/OverlayTexture;NO_OVERLAY:I", opcode = Opcodes.GETSTATIC))
	private static int replaceUvIndex(int original) {
		if (EntityRenderTintEvent.overlayOverride != null)
			return OverlayTexture.pack(15, 10); // TODO: store this info in a global alongside overlayOverride
		return original;
	}

}

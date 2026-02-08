package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patch to make {@link ItemStackRenderState} use a {@link RenderType} that allows uses Minecraft's overlay texture.
 *
 * @see UseOverlayableHeadFeatureRenderer
 */
@Mixin(ItemStackRenderState.LayerRenderState.class)
public class UseOverlayableItemRenderer {
	@ModifyExpressionValue(method = "submit", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;renderType:Lnet/minecraft/client/renderer/rendertype/RenderType;", opcode = Opcodes.GETFIELD))
	private RenderType replace(RenderType original) {
		RenderSetup.TextureBinding  binding;
		if (EntityRenderTintEvent.overlayOverride != null && (binding = original.state.textures.get("Sampler0")) != null)
			return RenderTypes.entityTranslucent(binding.location());
		return original;
	}
}

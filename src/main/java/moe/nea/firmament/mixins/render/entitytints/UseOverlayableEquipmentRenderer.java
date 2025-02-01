package moe.nea.firmament.mixins.render.entitytints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Patch to make {@link EquipmentRenderer} use a {@link RenderLayer} that allows uses Minecraft's overlay texture, if a {@link EntityRenderTintEvent#overlayOverride} is specified.
 */
@Mixin(EquipmentRenderer.class)
public class UseOverlayableEquipmentRenderer {
	@WrapOperation(method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
	private RenderLayer replace(Identifier texture, Operation<RenderLayer> original) {
		if (EntityRenderTintEvent.overlayOverride != null)
			return RenderLayer.getEntityTranslucent(texture);
		return original.call(texture);
	}

	@ModifyExpressionValue(method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/OverlayTexture;DEFAULT_UV:I"))
	private int replaceUvIndex(int original) {
		if (EntityRenderTintEvent.overlayOverride != null)
			return OverlayTexture.packUv(15, 10); // TODO: store this info in a global alongside overlayOverride
		return original;
	}
}

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.equipment.EquipmentModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentRenderer.class)
public class PatchLegacyArmorLayerSupport {
	@WrapOperation(method = "render(Lnet/minecraft/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/util/Identifier;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/equipment/EquipmentModelLoader;get(Lnet/minecraft/util/Identifier;)Lnet/minecraft/item/equipment/EquipmentModel;"))
	private EquipmentModel patchModelLayers(EquipmentModelLoader instance, Identifier id, Operation<EquipmentModel> original) {
		var modelOverride = CustomGlobalArmorOverrides.overrideArmorLayer(id);
		if (modelOverride != null) return modelOverride;
		return original.call(instance, id);
	}
}

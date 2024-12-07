package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: auto import legacy models, maybe!!! in a later patch tho
@Mixin(EquipmentRenderer.class)
public class PatchLegacyArmorLayerSupport {
	@WrapOperation(method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/equipment/EquipmentModelLoader;get(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/client/render/entity/equipment/EquipmentModel;"))
	private EquipmentModel patchModelLayers(EquipmentModelLoader instance, RegistryKey<EquipmentAsset> assetKey, Operation<EquipmentModel> original) {
		var modelOverride = CustomGlobalArmorOverrides.overrideArmorLayer(assetKey.getValue());
		if (modelOverride != null) return modelOverride;
		return original.call(instance, assetKey);
	}
}

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: auto import legacy models, maybe!!! in a later patch tho
@Mixin(EquipmentModelLoader.class)
public class PatchLegacyArmorLayerSupport {
	@Inject(method = "get", at = @At(value = "HEAD"), cancellable = true)
	private void patchModelLayers(RegistryKey<EquipmentAsset> assetKey, CallbackInfoReturnable<EquipmentModel> cir) {
		var modelOverride = CustomGlobalArmorOverrides.overrideArmorLayer(assetKey.getValue());
		if (modelOverride != null)
			cir.setReturnValue(modelOverride);
	}
}

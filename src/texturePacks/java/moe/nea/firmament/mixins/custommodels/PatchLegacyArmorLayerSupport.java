package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: auto import legacy models, maybe!!! in a later patch tho
@Mixin(EquipmentAssetManager.class)
public class PatchLegacyArmorLayerSupport {
	@Inject(method = "get", at = @At(value = "HEAD"), cancellable = true)
	private void patchModelLayers(ResourceKey<EquipmentAsset> assetKey, CallbackInfoReturnable<EquipmentClientInfo> cir) {
		var modelOverride = CustomGlobalArmorOverrides.overrideArmorLayer(assetKey.identifier());
		if (modelOverride != null)
			cir.setReturnValue(modelOverride);
	}
}

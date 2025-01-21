package moe.nea.firmament.mixins.custommodels;


import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import moe.nea.firmament.util.MC;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquipmentModel.Layer.class)
public class PatchLegacyTexturePathsIntoArmorLayers {
	@Shadow
	@Final
	private Identifier textureId;

	@Inject(method = "getFullTextureId", at = @At("HEAD"), cancellable = true)
	private void replaceWith1201TextureIfExists(EquipmentModel.LayerType layerType, CallbackInfoReturnable<Identifier> cir) {
		if (!CustomSkyBlockTextures.TConfig.INSTANCE.getEnableLegacyMinecraftCompat())
			return;
		var resourceManager = MC.INSTANCE.getResourceManager();
		// legacy format: "assets/{identifier.namespace}/textures/models/armor/{identifier.path}_layer_{isLegs ? 2 : 1}{suffix}.png"
		// suffix is sadly not available to us here. this means leather armor will look a bit shite
		var legacyIdentifier = this.textureId.withPath((textureName) -> {
			String var10000 = layerType.asString();
			return "textures/models/armor/" + textureName + "_layer_" +
				       (layerType == EquipmentModel.LayerType.HUMANOID_LEGGINGS ? 2 : 1)
				       + ".png";
		});
		if (resourceManager.getResource(legacyIdentifier).isPresent()) {
			cir.setReturnValue(legacyIdentifier);
		}
	}
}

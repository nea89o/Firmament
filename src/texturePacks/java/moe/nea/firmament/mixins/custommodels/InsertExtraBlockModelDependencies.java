package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelManager.class)
public class InsertExtraBlockModelDependencies {
	@Inject(method = "discoverModelDependencies", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelDiscovery;addSpecialModel(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/model/UnbakedModel;)V", shift = At.Shift.AFTER))
	private static void insertExtraModels(
            Map<Identifier, UnbakedModel> modelMap,
            BlockStateModelLoader.LoadedModels stateDefinition,
            ClientItemInfoLoader.LoadedClientInfos result,
            CallbackInfoReturnable cir, @Local ModelDiscovery modelsCollector) {
		CustomBlockTextures.collectExtraModels(modelsCollector);
	}
}

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BakedModelManager.class)
public class InsertExtraBlockModelDependencies {
	@Inject(method = "collect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ReferencedModelsCollector;addSpecialModel(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/UnbakedModel;)V", shift = At.Shift.AFTER))
	private static void insertExtraModels(
		Map<Identifier, UnbakedModel> modelMap,
		BlockStatesLoader.LoadedModels stateDefinition,
		ItemAssetsLoader.Result result,
		CallbackInfoReturnable cir, @Local ReferencedModelsCollector modelsCollector) {
		CustomBlockTextures.collectExtraModels(modelsCollector);
	}
}

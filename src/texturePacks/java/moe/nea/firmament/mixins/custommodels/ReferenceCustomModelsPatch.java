package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.BakeExtraModelsEvent;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BakedModelManager.class)
public abstract class ReferenceCustomModelsPatch {
	@Inject(method = "collect", at = @At("RETURN"))
	private static void addFirmamentReferencedModels(
		UnbakedModel missingModel, Map<Identifier, UnbakedModel> models, BlockStatesLoader.BlockStateDefinition blockStates, ItemAssetsLoader.Result itemAssets, CallbackInfoReturnable<ReferencedModelsCollector> cir,
		@Local ReferencedModelsCollector collector) {
		// TODO: Insert fake models based on firmskyblock models for a smoother transition

	}
}

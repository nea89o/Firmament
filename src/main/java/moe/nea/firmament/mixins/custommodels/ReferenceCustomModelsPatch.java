package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.events.BakeExtraModelsEvent;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ItemModel;
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

import java.util.Map;

@Mixin(ReferencedModelsCollector.class)
public abstract class ReferenceCustomModelsPatch {
	@Shadow
	protected abstract void addTopLevelModel(ModelIdentifier modelId, UnbakedModel model);

	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> inputs;

	@Inject(method = "addBlockStates", at = @At("RETURN"))
	private void addFirmamentReferencedModels(
		BlockStatesLoader.BlockStateDefinition definition, CallbackInfo ci
	) {
		BakeExtraModelsEvent.Companion.publish(new BakeExtraModelsEvent(
			(modelIdentifier, identifier) -> addTopLevelModel(modelIdentifier, new ItemModel(identifier))));

	}
}

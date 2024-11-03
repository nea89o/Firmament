package moe.nea.firmament.events

import java.util.function.BiConsumer
import net.minecraft.client.render.model.ReferencedModelsCollector
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

// TODO: Rename this event, since it is not really directly baking models anymore
class BakeExtraModelsEvent(
	private val addAnyModel: BiConsumer<ModelIdentifier, Identifier>,
) : FirmamentEvent() {

	fun addNonItemModel(modelIdentifier: ModelIdentifier, identifier: Identifier) {
		this.addAnyModel.accept(modelIdentifier, identifier)
	}

	fun addItemModel(modelIdentifier: ModelIdentifier) {
		addNonItemModel(
			modelIdentifier,
			modelIdentifier.id.withPrefixedPath(ReferencedModelsCollector.ITEM_DIRECTORY))
	}

	companion object : FirmamentEventBus<BakeExtraModelsEvent>()
}

package moe.nea.firmament.events

import java.util.function.BiConsumer
import net.minecraft.client.item.ItemAssetsLoader
import net.minecraft.client.render.model.ReferencedModelsCollector
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

// TODO: This event may be removed now since ItemAssetsLoader seems to load all item models now (probably to cope with servers setting the item_model component). Check whether this also applies to blocks now.
//@Deprecated(level = DeprecationLevel.ERROR, message = "This is no longer needed, since ItemAssetsLoader loads all item models.")
class BakeExtraModelsEvent(
	private val addAnyModel: BiConsumer<ModelIdentifier, Identifier>,
) : FirmamentEvent() {

	fun addNonItemModel(modelIdentifier: ModelIdentifier, identifier: Identifier) {
		this.addAnyModel.accept(modelIdentifier, identifier)
	}

	fun addItemModel(modelIdentifier: ModelIdentifier) {
	// TODO: If this is still needed: ItemAssetsLoader.FINDER
	//		addNonItemModel(
//			modelIdentifier,
//			modelIdentifier.id.withPrefixedPath())
	}

//	@Deprecated(level = DeprecationLevel.ERROR, message = "This is no longer needed, since ItemAssetsLoader loads all item models.")
	@Suppress("DEPRECATION")
	companion object : FirmamentEventBus<BakeExtraModelsEvent>()
}

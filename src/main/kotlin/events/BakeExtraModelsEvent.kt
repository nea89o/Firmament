
package moe.nea.firmament.events

import java.util.function.Consumer
import net.minecraft.client.util.ModelIdentifier

class BakeExtraModelsEvent(
    private val addItemModel: Consumer<ModelIdentifier>,
    private val addAnyModel: Consumer<ModelIdentifier>,
) : FirmamentEvent() {

    fun addNonItemModel(modelIdentifier: ModelIdentifier) {
        this.addAnyModel.accept(modelIdentifier)
    }

    fun addItemModel(modelIdentifier: ModelIdentifier) {
        this.addItemModel.accept(modelIdentifier)
    }

    companion object : FirmamentEventBus<BakeExtraModelsEvent>()
}

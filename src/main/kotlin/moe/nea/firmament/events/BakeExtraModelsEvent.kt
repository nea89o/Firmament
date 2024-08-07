
package moe.nea.firmament.events

import java.util.function.Consumer
import net.minecraft.client.util.ModelIdentifier

class BakeExtraModelsEvent(
    private val addModel: Consumer<ModelIdentifier>,
) : FirmamentEvent() {

    fun addModel(modelIdentifier: ModelIdentifier) {
        this.addModel.accept(modelIdentifier)
    }

    companion object : FirmamentEventBus<BakeExtraModelsEvent>()
}

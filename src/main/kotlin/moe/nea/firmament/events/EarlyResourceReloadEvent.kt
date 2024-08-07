
package moe.nea.firmament.events

import java.util.concurrent.Executor
import net.minecraft.resource.ResourceManager

data class EarlyResourceReloadEvent(val resourceManager: ResourceManager, val preparationExecutor: Executor) :
    FirmamentEvent() {
    companion object : FirmamentEventBus<EarlyResourceReloadEvent>()
}

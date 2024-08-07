
package moe.nea.firmament.events

import net.minecraft.resource.ReloadableResourceManagerImpl

data class FinalizeResourceManagerEvent(
    val resourceManager: ReloadableResourceManagerImpl,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<FinalizeResourceManagerEvent>()
}

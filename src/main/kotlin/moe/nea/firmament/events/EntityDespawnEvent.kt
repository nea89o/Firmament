
package moe.nea.firmament.events

import net.minecraft.entity.Entity

data class EntityDespawnEvent(
    val entity: Entity?, val entityId: Int,
    val reason: Entity.RemovalReason,
) : FirmamentEvent() {
    companion object: FirmamentEventBus<EntityDespawnEvent>()
}

package moe.nea.firmament.events

import net.minecraft.world.entity.Entity
import net.minecraft.world.InteractionHand

data class EntityInteractionEvent(
	val kind: InteractionKind,
	val entity: Entity,
	val hand: InteractionHand,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<EntityInteractionEvent>()
	enum class InteractionKind {
		/**
		 * Is sent when left-clicking an entity
		 */
		ATTACK,

        /**
         * Is sent when right-clicking an entity
         */
        INTERACT,
    }
}

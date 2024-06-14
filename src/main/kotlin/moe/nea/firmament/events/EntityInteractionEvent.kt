/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.entity.Entity
import net.minecraft.util.Hand

data class EntityInteractionEvent(
    val kind: InteractionKind,
    val entity: Entity,
    val hand: Hand,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<EntityInteractionEvent>()
    enum class InteractionKind {
        /**
         * Is sent when left-clicking an entity
         */
        ATTACK,

        /**
         * Is a fallback when [INTERACT_AT_LOCATION] fails
         */
        INTERACT,

        /**
         * Is tried first on right click
         */
        INTERACT_AT_LOCATION,
    }
}

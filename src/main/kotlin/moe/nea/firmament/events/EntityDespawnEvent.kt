/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.entity.Entity

data class EntityDespawnEvent(
    val entity: Entity?, val entityId: Int,
    val reason: Entity.RemovalReason,
) : FirmamentEvent() {
    companion object: FirmamentEventBus<EntityDespawnEvent>()
}

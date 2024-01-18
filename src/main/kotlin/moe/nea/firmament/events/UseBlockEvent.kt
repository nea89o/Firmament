/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

data class UseBlockEvent(val player: PlayerEntity, val world: World, val hand: Hand, val hitResult: BlockHitResult) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<UseBlockEvent>()
}

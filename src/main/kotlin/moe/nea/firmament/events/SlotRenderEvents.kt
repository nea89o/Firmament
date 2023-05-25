/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.events

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.slot.Slot

interface SlotRenderEvents {
    val matrices: MatrixStack
    val slot: Slot
    val mouseX: Int
    val mouseY: Int
    val delta: Float

    data class Before(
        override val matrices: MatrixStack, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<Before>()
    }

    data class After(
        override val matrices: MatrixStack, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<After>()
    }
}

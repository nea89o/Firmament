/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.slot.Slot

interface SlotRenderEvents {
    val context: DrawContext
    val slot: Slot
    val mouseX: Int
    val mouseY: Int
    val delta: Float

    data class Before(
        override val context: DrawContext, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<Before>()
    }

    data class After(
        override val context: DrawContext, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<After>()
    }
}

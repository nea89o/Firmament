/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.customgui

import net.minecraft.screen.slot.Slot

interface CoordRememberingSlot {
    fun rememberCoords_firmament()
    fun restoreCoords_firmament()
    fun getOriginalX_firmament(): Int
    fun getOriginalY_firmament(): Int
}

val Slot.originalX get() = (this as CoordRememberingSlot).getOriginalX_firmament()
val Slot.originalY get() = (this as CoordRememberingSlot).getOriginalY_firmament()

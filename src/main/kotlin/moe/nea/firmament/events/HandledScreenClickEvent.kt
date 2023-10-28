/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.screen.ingame.HandledScreen

data class HandledScreenClickEvent(val screen: HandledScreen<*>, val mouseX: Double, val mouseY: Double, val button: Int) :
    FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<HandledScreenClickEvent>()
}

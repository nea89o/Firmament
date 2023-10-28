/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.screen.Screen

data class ScreenChangeEvent(val old: Screen?, val new: Screen?) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<ScreenChangeEvent>()
}

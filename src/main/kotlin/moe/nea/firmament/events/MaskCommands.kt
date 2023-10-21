/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import com.mojang.brigadier.CommandDispatcher

data class MaskCommands(val dispatcher: CommandDispatcher<*>) : FirmamentEvent() {
    companion object : FirmamentEventBus<MaskCommands>()

    fun mask(name: String) {
        dispatcher.root.children.removeIf { it.name.equals(name, ignoreCase = true) }
    }
}

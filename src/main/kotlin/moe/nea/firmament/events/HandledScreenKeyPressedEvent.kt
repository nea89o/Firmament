/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.option.KeyBinding
import moe.nea.firmament.keybindings.IKeyBinding

data class HandledScreenKeyPressedEvent(
    val screen: HandledScreen<*>,
    val keyCode: Int,
    val scanCode: Int,
    val modifiers: Int
) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<HandledScreenKeyPressedEvent>()

    fun matches(keyBinding: KeyBinding): Boolean {
        return matches(IKeyBinding.minecraft(keyBinding))
    }

    fun matches(keyBinding: IKeyBinding): Boolean {
        return keyBinding.matches(keyCode, scanCode, modifiers)
    }
}

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.keybindings

import net.minecraft.client.option.KeyBinding

interface IKeyBinding {
    fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean

    fun withModifiers(wantedModifiers: Int): IKeyBinding {
        val old = this
        return object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                return old.matches(keyCode, scanCode, modifiers) && (modifiers and wantedModifiers) == wantedModifiers
            }
        }
    }

    companion object {
        fun minecraft(keyBinding: KeyBinding) = object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int) =
                keyBinding.matchesKey(keyCode, scanCode)
        }

        fun ofKeyCode(wantedKeyCode: Int) = object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = keyCode == wantedKeyCode
        }
    }
}

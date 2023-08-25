/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.keybindings

import kotlinx.serialization.Serializable
import org.lwjgl.glfw.GLFW

@Serializable
data class SavedKeyBinding(
    val keyCode: Int,
    val shift: Boolean = false,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
) : IKeyBinding {
    constructor(keyCode: Int, mods: Triple<Boolean, Boolean, Boolean>) : this(
        keyCode,
        mods.first && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT,
        mods.second && keyCode != GLFW.GLFW_KEY_LEFT_CONTROL && keyCode != GLFW.GLFW_KEY_RIGHT_CONTROL,
        mods.third && keyCode != GLFW.GLFW_KEY_LEFT_ALT && keyCode != GLFW.GLFW_KEY_RIGHT_ALT,
    )

    constructor(keyCode: Int, mods: Int) : this(keyCode, getMods(mods))

    companion object {
        fun getMods(modifiers: Int): Triple<Boolean, Boolean, Boolean> {
            return Triple(
                modifiers and GLFW.GLFW_MOD_SHIFT != 0,
                modifiers and GLFW.GLFW_MOD_CONTROL != 0,
                modifiers and GLFW.GLFW_MOD_ALT != 0
            )
        }
    }

    override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return keyCode == this.keyCode && getMods(modifiers) == Triple(shift, ctrl, alt)
    }

}

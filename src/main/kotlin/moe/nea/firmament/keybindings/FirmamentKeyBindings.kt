/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import moe.nea.firmament.gui.config.ManagedConfig

object FirmamentKeyBindings {
    fun registerKeyBinding(name: String, config: ManagedConfig) {
        val vanillaKeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                name,
                InputUtil.Type.KEYSYM,
                -1,
                "firmament.key.category"
            )
        )
        keyBindings[vanillaKeyBinding] = config
    }

    val keyBindings = mutableMapOf<KeyBinding, ManagedConfig>()

}

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

package moe.nea.firmament.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object FirmamentKeyBindings {
    val SLOT_LOCKING = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "firmament.key.slotlocking",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "firmament.key.category"
        )
    )
}

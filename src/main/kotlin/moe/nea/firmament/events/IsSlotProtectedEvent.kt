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

package moe.nea.firmament.events

import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC

data class IsSlotProtectedEvent(
    val slot: Slot, var isProtected: Boolean = false
) : FirmamentEvent() {
    companion object : FirmamentEventBus<IsSlotProtectedEvent>() {
        @JvmStatic
        fun shouldBlockInteraction(slot: Slot): Boolean {
            return publish(IsSlotProtectedEvent(slot)).isProtected.also {
                if (it) {
                    MC.player?.sendMessage(Text.translatable("firmament.protectitem").append(slot.stack.name))
                    CommonSoundEffects.playFailure()
                }
            }
        }
    }

    fun protect() {
        isProtected = true
    }
}

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

package moe.nea.firmament.util

import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

// TODO: Replace these with custom sound events that just re use the vanilla ogg s
object CommonSoundEffects {
    fun playSound(identifier: Identifier) {
        MC.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
    }

    fun playFailure() {
        playSound(Identifier("minecraft", "block.anvil.place"))
    }

    fun playSuccess() {
        playDing()
    }

    fun playDing() {
        playSound(Identifier("minecraft", "entity.arrow.hit_player"))
    }
}

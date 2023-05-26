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

import io.github.moulberry.repo.data.Coordinate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.util.math.BlockPos

object MC {
    inline val soundManager get() = MinecraftClient.getInstance().soundManager
    inline val player get() = MinecraftClient.getInstance().player
    inline val world get() = MinecraftClient.getInstance().world
    inline var screen
        get() = MinecraftClient.getInstance().currentScreen
        set(value) = MinecraftClient.getInstance().setScreen(value)
    inline val handledScreen: HandledScreen<*>? get() = MinecraftClient.getInstance().currentScreen as? HandledScreen<*>
    inline val window get() = MinecraftClient.getInstance().window
}


val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)

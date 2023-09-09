/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.util.math.BlockPos

object MC {
    fun sendCommand(command: String) {
        player?.networkHandler?.sendCommand(command)
    }

    inline val keyboard get() = MinecraftClient.getInstance().keyboard
    inline val textureManager get() = MinecraftClient.getInstance().textureManager
    inline val inGameHud get() = MinecraftClient.getInstance().inGameHud
    inline val font get() = MinecraftClient.getInstance().textRenderer
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

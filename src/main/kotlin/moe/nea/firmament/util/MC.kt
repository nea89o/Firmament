/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen

object MC {

    private val messageQueue = ConcurrentLinkedQueue<Text>()

    init {
        TickEvent.subscribe {
            while (true) {
                inGameHud.chatHud.addMessage(messageQueue.poll() ?: break)
            }
        }
    }

    fun sendChat(text: Text) {
        if (instance.isOnThread)
            inGameHud.chatHud.addMessage(text)
        else
            messageQueue.add(text)
    }

    fun sendCommand(command: String) {
        player?.networkHandler?.sendCommand(command)
    }

    inline val instance get() = MinecraftClient.getInstance()
    inline val keyboard get() = MinecraftClient.getInstance().keyboard
    inline val textureManager get() = MinecraftClient.getInstance().textureManager
    inline val inGameHud get() = MinecraftClient.getInstance().inGameHud
    inline val font get() = MinecraftClient.getInstance().textRenderer
    inline val soundManager get() = MinecraftClient.getInstance().soundManager
    inline val player get() = MinecraftClient.getInstance().player
    inline val camera get() = MinecraftClient.getInstance().cameraEntity
    inline val guiAtlasManager get() = MinecraftClient.getInstance().guiAtlasManager
    inline val world get() = MinecraftClient.getInstance().world
    inline var screen
        get() = MinecraftClient.getInstance().currentScreen
        set(value) = MinecraftClient.getInstance().setScreen(value)
    inline val handledScreen: HandledScreen<*>? get() = MinecraftClient.getInstance().currentScreen as? HandledScreen<*>
    inline val window get() = MinecraftClient.getInstance().window
}


val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)

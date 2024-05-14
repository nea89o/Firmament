/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.events.TickEvent

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

    fun sendServerCommand(command: String) {
        val nh = player?.networkHandler ?: return
        nh.sendPacket(
            CommandExecutionC2SPacket(
                command,
            )
        )
    }

    fun sendServerChat(text: String) {
        player?.networkHandler?.sendChatMessage(text)
    }

    fun sendCommand(command: String) {
        player?.networkHandler?.sendCommand(command)
    }

    inline val resourceManager get() = (MinecraftClient.getInstance().resourceManager as ReloadableResourceManagerImpl)
    inline val networkHandler get() = player?.networkHandler
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
    inline val currentRegistries: RegistryWrapper.WrapperLookup? get() = world?.registryManager
    val defaultRegistries: RegistryWrapper.WrapperLookup = BuiltinRegistries.createWrapperLookup()
    val defaultItems = defaultRegistries.getWrapperOrThrow(RegistryKeys.ITEM)
}


val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)

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

    fun onMainThread(block: () -> Unit) {
        if (instance.isOnThread)
            block()
        else
            instance.send(block)
    }

    inline val resourceManager get() = (instance.resourceManager as ReloadableResourceManagerImpl)
    inline val networkHandler get() = player?.networkHandler
    inline val instance get() = MinecraftClient.getInstance()
    inline val keyboard get() = instance.keyboard
    inline val textureManager get() = instance.textureManager
    inline val inGameHud get() = instance.inGameHud
    inline val font get() = instance.textRenderer
    inline val soundManager get() = instance.soundManager
    inline val player get() = instance.player
    inline val camera get() = instance.cameraEntity
    inline val guiAtlasManager get() = instance.guiAtlasManager
    inline val world get() = instance.world
    inline var screen
        get() = instance.currentScreen
        set(value) = instance.setScreen(value)
    inline val handledScreen: HandledScreen<*>? get() = instance.currentScreen as? HandledScreen<*>
    inline val window get() = instance.window
    inline val currentRegistries: RegistryWrapper.WrapperLookup? get() = world?.registryManager
    val defaultRegistries: RegistryWrapper.WrapperLookup = BuiltinRegistries.createWrapperLookup()
    val defaultItems = defaultRegistries.getWrapperOrThrow(RegistryKeys.ITEM)
}


val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)

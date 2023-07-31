/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import org.lwjgl.glfw.GLFW
import kotlin.io.path.absolute
import kotlin.io.path.exists
import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.keybindings.IKeyBinding
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.iterate
import moe.nea.firmament.util.skyBlockId

object DeveloperFeatures : FirmamentFeature {
    override val identifier: String
        get() = "developer"
    override val config: TConfig
        get() = TConfig
    override val defaultEnabled: Boolean
        get() = Firmament.DEBUG

    val gradleDir =
        Path.of(".").absolute()
            .iterate { it.parent }
            .find { it.resolve("settings.gradle.kts").exists() }

    object TConfig : ManagedConfig("developer") {
        val autoRebuildResources by toggle("auto-rebuild") { false }
    }

    @JvmStatic
    fun hookOnBeforeResourceReload(client: MinecraftClient): CompletableFuture<Void> {
        val reloadFuture = if (TConfig.autoRebuildResources && isEnabled && gradleDir != null) {
            val builder = ProcessBuilder("./gradlew", ":processResources")
            builder.directory(gradleDir.toFile())
            builder.inheritIO()
            val process = builder.start()
            MC.player?.sendMessage(Text.translatable("firmament.dev.resourcerebuild.start"))
            val startTime = TimeMark.now()
            process.toHandle().onExit().thenApply {
                MC.player?.sendMessage(Text.translatable("firmament.dev.resourcerebuild.done", startTime.passedTime()))
                Unit
            }
        } else {
            CompletableFuture.completedFuture(Unit)
        }
        return reloadFuture.thenCompose { client.reloadResources() }
    }

    override fun onLoad() {
        HandledScreenKeyPressedEvent.subscribe {
            if (it.matches(IKeyBinding.ofKeyCode(GLFW.GLFW_KEY_K))) {
                it.screen as AccessorHandledScreen
                val focussedSlot = it.screen.focusedSlot_NEU ?: return@subscribe
                val item = focussedSlot.stack ?: return@subscribe
                val ident = item.skyBlockId?.identifier.toString()
                MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                    Text.translatable(
                        "firmament.debug.skyblockid",
                        ident
                    ).setStyle(
                        Style.EMPTY.withColor(Formatting.AQUA)
                            .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ident))
                            .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("firmament.debug.skyblockid.copy")))
                    )
                )
            }
        }
    }
}

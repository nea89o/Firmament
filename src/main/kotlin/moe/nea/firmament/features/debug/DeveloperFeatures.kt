/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.absolute
import kotlin.io.path.exists
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.errorBoundary
import moe.nea.firmament.util.iterate

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
                MC.player?.sendMessage(Text.stringifiedTranslatable("firmament.dev.resourcerebuild.done", startTime.passedTime()))
                Unit
            }
        } else {
            CompletableFuture.completedFuture(Unit)
        }
        return reloadFuture.thenCompose { client.reloadResources() }
    }


    override fun onLoad() {
    }
}


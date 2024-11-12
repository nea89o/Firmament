package moe.nea.firmament.features.debug

import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlinx.serialization.json.encodeToStream
import kotlin.io.path.absolute
import kotlin.io.path.exists
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
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

	object TConfig : ManagedConfig("developer", Category.DEV) {
		val autoRebuildResources by toggle("auto-rebuild") { false }
	}

	var missingTranslations: Set<String>? = null

	@JvmStatic
	fun hookMissingTranslations(missingTranslations: Set<String>) {
		this.missingTranslations = missingTranslations
	}

	@Subscribe
	fun dumpMissingTranslations(tickEvent: TickEvent) {
		val toDump = missingTranslations ?: return
		missingTranslations = null
		File("missing_translations.json").outputStream().use {
			Firmament.json.encodeToStream(toDump.associateWith { "Mis" + "sing translation" }, it)
		}
	}

	@JvmStatic
	fun hookOnBeforeResourceReload(client: MinecraftClient): CompletableFuture<Void> {
		val reloadFuture = if (TConfig.autoRebuildResources && isEnabled && gradleDir != null) {
			val builder = ProcessBuilder("./gradlew", ":processResources")
			builder.directory(gradleDir.toFile())
			builder.inheritIO()
			val process = builder.start()
			MC.sendChat(Text.translatable("firmament.dev.resourcerebuild.start"))
			val startTime = TimeMark.now()
			process.toHandle().onExit().thenApply {
				MC.sendChat(Text.stringifiedTranslatable(
					"firmament.dev.resourcerebuild.done",
					startTime.passedTime()))
				Unit
			}
		} else {
			CompletableFuture.completedFuture(Unit)
		}
		return reloadFuture.thenCompose { client.reloadResources() }
	}
}


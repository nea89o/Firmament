package moe.nea.firmament.features.texturepack

import java.util.Optional
import kotlinx.serialization.Serializable
import kotlin.jvm.optionals.getOrNull
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.util.collections.WeakCache

object CustomTextColors : SinglePreparationResourceReloader<CustomTextColors.TextOverrides?>() {
	@Serializable
	data class TextOverrides(
		val defaultColor: Int,
		val overrides: List<TextOverride> = listOf()
	)

	@Serializable
	data class TextOverride(
		val predicate: StringMatcher,
		val override: Int,
	)

	@Subscribe
	fun registerTextColorReloader(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloader(this)
	}

	val cache = WeakCache.memoize<Text, Optional<Int>>("CustomTextColor") { text ->
		val override = textOverrides ?: return@memoize Optional.empty()
		Optional.of(override.overrides.find { it.predicate.matches(text) }?.override ?: override.defaultColor)
	}

	fun mapTextColor(text: Text, oldColor: Int): Int {
		if (textOverrides == null) return oldColor
		return cache(text).getOrNull() ?: oldColor
	}

	override fun prepare(
		manager: ResourceManager,
		profiler: Profiler
	): TextOverrides? {
		val resource = manager.getResource(Identifier.of("firmskyblock", "overrides/text_colors.json")).getOrNull()
			?: return null
		return Firmament.tryDecodeJsonFromStream<TextOverrides>(resource.inputStream)
			.getOrElse {
				Firmament.logger.error("Could not parse text_colors.json", it)
				null
			}
	}

	var textOverrides: TextOverrides? = null

	override fun apply(
		prepared: TextOverrides?,
		manager: ResourceManager,
		profiler: Profiler
	) {
		textOverrides = prepared
	}
}

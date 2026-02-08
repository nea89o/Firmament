package moe.nea.firmament.features.texturepack

import java.util.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.optionals.getOrNull
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.profiling.ProfilerFiller
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.intoOptional

object CustomTextColors : SimplePreparableReloadListener<Optional<CustomTextColors.TextOverrides>>() {
	@Serializable
	data class TextOverrides(
		val defaultColor: Int,
		val overrides: List<TextOverride> = listOf()
	) {
		/**
		 * Stub custom text color to allow always returning a text override
		 */
		@Transient
		val baseOverride = TextOverride(
			StringMatcher.Equals("", false),
			defaultColor,
			0,
			0
		)
	}

	@Serializable
	data class TextOverride(
		val predicate: StringMatcher,
		val override: Int,
		val x: Int = 0,
		val y: Int = 0,
	)

	@Subscribe
	fun registerTextColorReloader(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloadListener(this)
	}

	val cache = WeakCache.memoize<Component, Optional<TextOverride>>("CustomTextColor") { text ->
		val override = textOverrides ?: return@memoize Optional.empty()
		Optional.ofNullable(override.overrides.find { it.predicate.matches(text) })
	}

	fun mapTextColor(text: Component, oldColor: Int): Int {
		val override = cache(text).orElse(null)
		return override?.override ?: textOverrides?.defaultColor ?: oldColor
	}

	override fun prepare(
        manager: ResourceManager,
        profiler: ProfilerFiller
	): Optional<TextOverrides> {
		val resource = manager.getResource(Identifier.fromNamespaceAndPath("firmskyblock", "overrides/text_colors.json")).getOrNull()
			?: return Optional.empty()
		return Firmament.tryDecodeJsonFromStream<TextOverrides>(resource.open())
			.getOrElse {
				Firmament.logger.error("Could not parse text_colors.json", it)
				null
			}.intoOptional()
	}

	var textOverrides: TextOverrides? = null

	override fun apply(
        prepared: Optional<TextOverrides>,
        manager: ResourceManager,
        profiler: ProfilerFiller
	) {
		textOverrides = prepared.getOrNull()
	}
}

package moe.nea.firmament.features.texturepack

import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import java.util.Optional
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.jvm.optionals.getOrNull
import net.minecraft.block.SkullBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.component.type.ProfileComponent
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.mc.decodeProfileTextureProperty
import moe.nea.firmament.util.skyBlockId

object CustomSkyBlockTextures : FirmamentFeature {
	override val identifier: String
		get() = "custom-skyblock-textures"

	object TConfig : ManagedConfig(identifier, Category.INTEGRATIONS) { // TODO: should this be its own thing?
		val enabled by toggle("enabled") { true }
		val skullsEnabled by toggle("skulls-enabled") { true }
		val cacheForever by toggle("cache-forever") { true }
		val cacheDuration by integer("cache-duration", 0, 100) { 1 }
		val enableModelOverrides by toggle("model-overrides") { true }
		val enableArmorOverrides by toggle("armor-overrides") { true }
		val enableBlockOverrides by toggle("block-overrides") { true }
		val enableLegacyMinecraftCompat by toggle("legacy-minecraft-path-support") { true }
		val enableLegacyCIT by toggle("legacy-cit") { true }
		val allowRecoloringUiText by toggle("recolor-text") { true }
	}

	override val config: ManagedConfig
		get() = TConfig

	val allItemCaches by lazy {
		listOf(
			skullTextureCache.cache,
			CustomItemModelEvent.cache.cache,
			CustomGlobalArmorOverrides.overrideCache.cache
		)
	}

	init {
		PowerUserTools.getSkullId = ::getSkullTexture
	}

	fun clearAllCaches() {
		allItemCaches.forEach(WeakCache<*, *, *>::clear)
	}

	@Subscribe
	fun onTick(it: TickEvent) {
		if (TConfig.cacheForever) return
		if (TConfig.cacheDuration < 1 || it.tickCount % TConfig.cacheDuration == 0) {
			clearAllCaches()
		}
	}

	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		event.registerOnApply("Clear firmament CIT caches") {
			clearAllCaches()
		}
	}

	@Subscribe
	fun onCustomModelId(it: CustomItemModelEvent) {
		if (!TConfig.enabled) return
		val id = it.itemStack.skyBlockId ?: return
		it.overrideIfExists(Identifier.of("firmskyblock", id.identifier.path))
	}

	private val skullTextureCache =
		WeakCache.memoize<ProfileComponent, Optional<Identifier>>("SkullTextureCache") { component ->
			val id = getSkullTexture(component) ?: return@memoize Optional.empty()
			if (!MinecraftClient.getInstance().resourceManager.getResource(id).isPresent) {
				return@memoize Optional.empty()
			}
			return@memoize Optional.of(id)
		}

	private val mcUrlRegex = "https?://textures.minecraft.net/texture/([a-fA-F0-9]+)".toRegex()

	fun getSkullId(textureProperty: Property): String? {
		val texture = decodeProfileTextureProperty(textureProperty) ?: return null
		val textureUrl =
			texture.textures[MinecraftProfileTexture.Type.SKIN]?.url ?: return null
		val mcUrlData = mcUrlRegex.matchEntire(textureUrl) ?: return null
		return mcUrlData.groupValues[1]
	}

	fun getSkullTexture(profile: ProfileComponent): Identifier? {
		val id = getSkullId(profile.properties["textures"].firstOrNull() ?: return null) ?: return null
		return Identifier.of("firmskyblock", "textures/placedskull/$id.png")
	}

	fun modifySkullTexture(
		type: SkullBlock.SkullType?,
		component: ProfileComponent?,
		cir: CallbackInfoReturnable<RenderLayer>
	) {
		if (type != SkullBlock.Type.PLAYER) return
		if (!TConfig.skullsEnabled) return
		if (component == null) return

		val n = skullTextureCache.invoke(component).getOrNull() ?: return
		cir.returnValue = RenderLayer.getEntityTranslucent(n)
	}
}

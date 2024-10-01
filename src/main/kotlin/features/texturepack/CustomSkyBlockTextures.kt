package moe.nea.firmament.features.texturepack

import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import java.util.Optional
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.jvm.optionals.getOrNull
import net.minecraft.block.SkullBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.component.type.ProfileComponent
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.BakeExtraModelsEvent
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.mc.decodeProfileTextureProperty
import moe.nea.firmament.util.skyBlockId

object CustomSkyBlockTextures : FirmamentFeature {
    override val identifier: String
        get() = "custom-skyblock-textures"

    object TConfig : ManagedConfig(identifier) {
        val enabled by toggle("enabled") { true }
        val skullsEnabled by toggle("skulls-enabled") { true }
        val cacheForever by toggle("cache-forever") { true }
        val cacheDuration by integer("cache-duration", 0, 100) { 1 }
        val enableModelOverrides by toggle("model-overrides") { true }
        val enableArmorOverrides by toggle("armor-overrides") { true }
        val enableBlockOverrides by toggle("block-overrides") { true }
        val enableLegacyCIT by toggle("legacy-cit") { true }
    }

    override val config: ManagedConfig
        get() = TConfig

    val allItemCaches by lazy {
        listOf(
            CustomItemModelEvent.cache.cache,
            skullTextureCache.cache,
            CustomGlobalTextures.overrideCache.cache,
            CustomGlobalArmorOverrides.overrideCache.cache
        )
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
    fun bakeCustomFirmModels(event: BakeExtraModelsEvent) {
        val resources =
            MinecraftClient.getInstance().resourceManager.findResources("models/item"
            ) { it: Identifier ->
                "firmskyblock" == it.namespace && it.path
                    .endsWith(".json")
            }
        for (identifier in resources.keys) {
            val modelId = ModelIdentifier.ofInventoryVariant(
                Identifier.of(
                    "firmskyblock",
                    identifier.path.substring(
                        "models/item/".length,
                        identifier.path.length - ".json".length),
                ))
            event.addItemModel(modelId)
        }
    }

    @Subscribe
    fun onCustomModelId(it: CustomItemModelEvent) {
        if (!TConfig.enabled) return
        val id = it.itemStack.skyBlockId ?: return
        it.overrideModel = ModelIdentifier.ofInventoryVariant(Identifier.of("firmskyblock", id.identifier.path))
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

@file:UseSerializers(IdentifierSerializer::class)

package moe.nea.firmament.features.texturepack

import java.util.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.texturepack.CustomGlobalTextures.logger
import moe.nea.firmament.util.IdentifierSerializer
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.skyBlockId

object CustomGlobalArmorOverrides : SubscriptionOwner {
    @Serializable
    data class ArmorOverride(
        @SerialName("item_ids")
        val itemIds: List<String>,
        val layers: List<ArmorOverrideLayer>,
        val overrides: List<ArmorOverrideOverride> = listOf(),
    ) {
        @Transient
        val bakedLayers = bakeLayers(layers)
    }

    fun bakeLayers(layers: List<ArmorOverrideLayer>): List<ArmorMaterial.Layer> {
        return layers.map { ArmorMaterial.Layer(it.identifier, it.suffix, it.tint) }
    }

    @Serializable
    data class ArmorOverrideLayer(
        val tint: Boolean = false,
        val identifier: Identifier,
        val suffix: String = "",
    )

    @Serializable
    data class ArmorOverrideOverride(
        val predicate: FirmamentModelPredicate,
        val layers: List<ArmorOverrideLayer>,
    ) {
        @Transient
        val bakedLayers = bakeLayers(layers)
    }

    override val delegateFeature: FirmamentFeature
        get() = CustomSkyBlockTextures

    val overrideCache = WeakCache.memoize<ItemStack, Optional<List<ArmorMaterial.Layer>>>("ArmorOverrides") { stack ->
        val id = stack.skyBlockId ?: return@memoize Optional.empty()
        val override = overrides[id.neuItem] ?: return@memoize Optional.empty()
        for (suboverride in override.overrides) {
            if (suboverride.predicate.test(stack)) {
                return@memoize Optional.of(suboverride.bakedLayers)
            }
        }
        return@memoize Optional.of(override.bakedLayers)
    }

    @JvmStatic
    fun overrideArmor(stack: ItemStack): List<ArmorMaterial.Layer>? {
        if (!CustomSkyBlockTextures.TConfig.enableArmorOverrides) return null
        return overrideCache.invoke(stack).getOrNull()
    }

    var overrides: Map<String, ArmorOverride> = mapOf()

    @Subscribe
    fun onStart(event: FinalizeResourceManagerEvent) {
        event.resourceManager.registerReloader(object :
                                                   SinglePreparationResourceReloader<Map<String, ArmorOverride>>() {
            override fun prepare(manager: ResourceManager, profiler: Profiler): Map<String, ArmorOverride> {
                val overrideFiles = manager.findResources("overrides/armor_models") {
                    it.namespace == "firmskyblock" && it.path.endsWith(".json")
                }
                val overrides = overrideFiles.mapNotNull {
                    Firmament.tryDecodeJsonFromStream<ArmorOverride>(it.value.inputStream).getOrElse { ex ->
                        logger.error("Failed to load armor texture override at ${it.key}", ex)
                        null
                    }
                }
                val associatedMap = overrides.flatMap { obj -> obj.itemIds.map { it to obj } }
                    .toMap()
                return associatedMap
            }

            override fun apply(prepared: Map<String, ArmorOverride>, manager: ResourceManager, profiler: Profiler) {
                overrides = prepared
            }
        })
    }

}

@file:UseSerializers(BlockPosSerializer::class, IdentifierSerializer::class)

package moe.nea.firmament.features.texturepack

import java.util.concurrent.CompletableFuture
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kotlin.jvm.optionals.getOrNull
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.BakeExtraModelsEvent
import moe.nea.firmament.events.EarlyResourceReloadEvent
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.features.texturepack.CustomGlobalTextures.logger
import moe.nea.firmament.util.IdentifierSerializer
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.json.BlockPosSerializer
import moe.nea.firmament.util.json.SingletonSerializableList


object CustomBlockTextures {
    @Serializable
    data class CustomBlockOverride(
        val modes: @Serializable(SingletonSerializableList::class) List<String>,
        val area: List<Area>? = null,
        val replacements: Map<Identifier, Replacement>,
    )

    @Serializable(with = Replacement.Serializer::class)
    data class Replacement(
        val block: Identifier,
        val sound: Identifier?,
    ) {

        @Transient
        val blockModelIdentifier get() = ModelIdentifier(block.withPrefixedPath("block/"), "firmament")

        @Transient
        val bakedModel: BakedModel by lazy(LazyThreadSafetyMode.NONE) {
            MC.instance.bakedModelManager.getModel(blockModelIdentifier)
        }

        @OptIn(ExperimentalSerializationApi::class)
        @kotlinx.serialization.Serializer(Replacement::class)
        object DefaultSerializer : KSerializer<Replacement>

        object Serializer : KSerializer<Replacement> {
            val delegate = serializer<JsonElement>()
            override val descriptor: SerialDescriptor
                get() = delegate.descriptor

            override fun deserialize(decoder: Decoder): Replacement {
                val jsonElement = decoder.decodeSerializableValue(delegate)
                if (jsonElement is JsonPrimitive) {
                    require(jsonElement.isString)
                    return Replacement(Identifier.tryParse(jsonElement.content)!!, null)
                }
                return (decoder as JsonDecoder).json.decodeFromJsonElement(DefaultSerializer, jsonElement)
            }

            override fun serialize(encoder: Encoder, value: Replacement) {
                encoder.encodeSerializableValue(DefaultSerializer, value)
            }
        }
    }

    @Serializable
    data class Area(
        val min: BlockPos,
        val max: BlockPos,
    ) {
        @Transient
        val realMin = BlockPos(
            minOf(min.x, max.x),
            minOf(min.y, max.y),
            minOf(min.z, max.z),
        )

        @Transient
        val realMax = BlockPos(
            maxOf(min.x, max.x),
            maxOf(min.y, max.y),
            maxOf(min.z, max.z),
        )

        fun roughJoin(other: Area): Area {
            return Area(
                BlockPos(
                    minOf(realMin.x, other.realMin.x),
                    minOf(realMin.y, other.realMin.y),
                    minOf(realMin.z, other.realMin.z),
                ),
                BlockPos(
                    maxOf(realMax.x, other.realMax.x),
                    maxOf(realMax.y, other.realMax.y),
                    maxOf(realMax.z, other.realMax.z),
                )
            )
        }

        fun contains(blockPos: BlockPos): Boolean {
            return (blockPos.x in realMin.x..realMax.x) &&
                (blockPos.y in realMin.y..realMax.y) &&
                (blockPos.z in realMin.z..realMax.z)
        }
    }

    data class LocationReplacements(
        val lookup: Map<Block, List<BlockReplacement>>
    )

    data class BlockReplacement(
        val checks: List<Area>?,
        val replacement: Replacement,
    ) {
        val roughCheck by lazy(LazyThreadSafetyMode.NONE) {
            if (checks == null || checks.size < 3) return@lazy null
            checks.reduce { acc, next -> acc.roughJoin(next) }
        }
    }

    data class BakedReplacements(val data: Map<SkyBlockIsland, LocationReplacements>)

    var allLocationReplacements: BakedReplacements = BakedReplacements(mapOf())
    var currentIslandReplacements: LocationReplacements? = null

    fun refreshReplacements() {
        val location = SBData.skyblockLocation
        val replacements =
            if (CustomSkyBlockTextures.TConfig.enableBlockOverrides) location?.let(allLocationReplacements.data::get)
            else null
        val lastReplacements = currentIslandReplacements
        currentIslandReplacements = replacements
        if (lastReplacements != replacements) {
            MC.nextTick {
                MC.worldRenderer.chunks?.chunks?.forEach {
                    // false schedules rebuilds outside a 27 block radius to happen async
                    it.scheduleRebuild(false)
                }
            }
        }
    }

    fun matchesPosition(replacement: BlockReplacement, blockPos: BlockPos?): Boolean {
        if (blockPos == null) return true
        val rc = replacement.roughCheck
        if (rc != null && !rc.contains(blockPos)) return false
        val areas = replacement.checks
        if (areas != null && !areas.any { it.contains(blockPos) }) return false
        return true
    }

    @JvmStatic
    fun getReplacementModel(block: BlockState, blockPos: BlockPos?): BakedModel? {
        return getReplacement(block, blockPos)?.bakedModel
    }

    @JvmStatic
    fun getReplacement(block: BlockState, blockPos: BlockPos?): Replacement? {
        if (isInFallback() && blockPos == null) return null
        val replacements = currentIslandReplacements?.lookup?.get(block.block) ?: return null
        for (replacement in replacements) {
            if (replacement.checks == null || matchesPosition(replacement, blockPos))
                return replacement.replacement
        }
        return null
    }


    @Subscribe
    fun onLocation(event: SkyblockServerUpdateEvent) {
        refreshReplacements()
    }

    @Volatile
    var preparationFuture: CompletableFuture<BakedReplacements> = CompletableFuture.completedFuture(BakedReplacements(
        mapOf()))

    val insideFallbackCall = ThreadLocal.withInitial { 0 }

    @JvmStatic
    fun enterFallbackCall() {
        insideFallbackCall.set(insideFallbackCall.get() + 1)
    }

    fun isInFallback() = insideFallbackCall.get() > 0

    @JvmStatic
    fun exitFallbackCall() {
        insideFallbackCall.set(insideFallbackCall.get() - 1)
    }

    @Subscribe
    fun onEarlyReload(event: EarlyResourceReloadEvent) {
        preparationFuture = CompletableFuture
            .supplyAsync(
                { prepare(event.resourceManager) }, event.preparationExecutor)
    }

    @Subscribe
    fun bakeExtraModels(event: BakeExtraModelsEvent) {
        preparationFuture.join().data.values
            .flatMap { it.lookup.values }
            .flatten()
            .mapTo(mutableSetOf()) { it.replacement.blockModelIdentifier }
            .forEach { event.addNonItemModel(it) }
    }

    private fun prepare(manager: ResourceManager): BakedReplacements {
        val resources = manager.findResources("overrides/blocks") {
            it.namespace == "firmskyblock" && it.path.endsWith(".json")
        }
        val map = mutableMapOf<SkyBlockIsland, MutableMap<Block, MutableList<BlockReplacement>>>()
        for ((file, resource) in resources) {
            val json =
                Firmament.tryDecodeJsonFromStream<CustomBlockOverride>(resource.inputStream)
                    .getOrElse { ex ->
                        logger.error("Failed to load block texture override at $file", ex)
                        continue
                    }
            for (mode in json.modes) {
                val island = SkyBlockIsland.forMode(mode)
                val islandMpa = map.getOrPut(island, ::mutableMapOf)
                for ((blockId, replacement) in json.replacements) {
                    val block = MC.defaultRegistries.getWrapperOrThrow(RegistryKeys.BLOCK)
                        .getOptional(RegistryKey.of(RegistryKeys.BLOCK, blockId))
                        .getOrNull()
                    if (block == null) {
                        logger.error("Failed to load block texture override at ${file}: unknown block '$blockId'")
                        continue
                    }
                    val replacements = islandMpa.getOrPut(block.value(), ::mutableListOf)
                    replacements.add(BlockReplacement(json.area, replacement))
                }
            }
        }

        return BakedReplacements(map.mapValues { LocationReplacements(it.value) })
    }

    @JvmStatic
    fun patchIndigo(orig: BakedModel, pos: BlockPos, state: BlockState): BakedModel {
        return getReplacementModel(state, pos) ?: orig
    }

    @Subscribe
    fun onStart(event: FinalizeResourceManagerEvent) {
        event.resourceManager.registerReloader(object :
                                                   SinglePreparationResourceReloader<BakedReplacements>() {
            override fun prepare(manager: ResourceManager, profiler: Profiler): BakedReplacements {
                return preparationFuture.join()
            }

            override fun apply(prepared: BakedReplacements, manager: ResourceManager, profiler: Profiler?) {
                allLocationReplacements = prepared
                refreshReplacements()
            }
        })
    }
}

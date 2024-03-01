/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Lifecycle
import java.util.*
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.item.map.MapState
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.entry.RegistryEntryOwner
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.featuretoggle.FeatureFlag
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.TypeFilter
import net.minecraft.util.function.LazyIterationConsumer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.util.profiler.DummyProfiler
import net.minecraft.world.BlockView
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import net.minecraft.world.MutableWorldProperties
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkManager
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.EmptyChunk
import net.minecraft.world.chunk.light.LightingProvider
import net.minecraft.world.entity.EntityLookup
import net.minecraft.world.event.GameEvent
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.QueryableTickScheduler
import net.minecraft.world.tick.TickManager

fun <T> makeRegistry(registryWrapper: RegistryWrapper.Impl<T>, key: RegistryKey<out Registry<T>>): Registry<T> {
    val inverseLookup = registryWrapper.streamEntries()
        .asSequence().map { it.value() to it.registryKey() }
        .toMap()
    val idLookup = registryWrapper.streamEntries()
        .asSequence()
        .map { it.registryKey() }
        .withIndex()
        .associate { it.value to it.index }
    val map = registryWrapper.streamEntries().asSequence().map { it.registryKey() to it.value() }.toMap(mutableMapOf())
    val inverseIdLookup = idLookup.asIterable().associate { (k, v) -> v to k }
    return object : Registry<T> {
        override fun get(key: RegistryKey<T>?): T? {
            return registryWrapper.getOptional(key).getOrNull()?.value()
        }

        override fun iterator(): MutableIterator<T> {
            return object : MutableIterator<T> {
                val iterator = registryWrapper.streamEntries().iterator()
                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): T {
                    return iterator.next().value()
                }

                override fun remove() {
                    TODO("Not yet implemented")
                }
            }
        }

        override fun getRawId(value: T?): Int {
            return idLookup[inverseLookup[value ?: return -1] ?: return -1] ?: return -1
        }

        override fun get(id: Identifier?): T? {
            return get(RegistryKey.of(key, id))
        }

        override fun get(index: Int): T? {
            return get(inverseIdLookup[index] ?: return null)
        }

        override fun size(): Int {
            return idLookup.size
        }

        override fun getKey(): RegistryKey<out Registry<T>> {
            return key
        }

        override fun getLifecycle(): Lifecycle {
            return Lifecycle.stable()
        }

        override fun getIds(): MutableSet<Identifier> {
            return idLookup.keys.mapTo(mutableSetOf()) { it.value }
        }

        override fun getEntrySet(): MutableSet<MutableMap.MutableEntry<RegistryKey<T>, T>> {
            return map.entries
        }

        override fun getKeys(): MutableSet<RegistryKey<T>> {
            return map.keys
        }

        override fun getRandom(random: Random?): Optional<RegistryEntry.Reference<T>> {
            return registryWrapper.streamEntries().findFirst()
        }

        override fun containsId(id: Identifier?): Boolean {
            return idLookup.containsKey(RegistryKey.of(key, id ?: return false))
        }

        override fun freeze(): Registry<T> {
            return this
        }

        override fun getEntry(rawId: Int): Optional<RegistryEntry.Reference<T>> {
            val x = inverseIdLookup[rawId] ?: return Optional.empty()
            return Optional.of(RegistryEntry.Reference.standAlone(registryWrapper, x))
        }

        override fun streamEntries(): Stream<RegistryEntry.Reference<T>> {
            return registryWrapper.streamEntries()
        }

        override fun streamTagsAndEntries(): Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> {
            return streamTags().map { Pair(it, getOrCreateEntryList(it)) }
        }

        override fun streamTags(): Stream<TagKey<T>> {
            return registryWrapper.streamTagKeys()
        }

        override fun clearTags() {
        }

        override fun getEntryOwner(): RegistryEntryOwner<T> {
            return registryWrapper
        }

        override fun getReadOnlyWrapper(): RegistryWrapper.Impl<T> {
            return registryWrapper
        }

        override fun populateTags(tagEntries: MutableMap<TagKey<T>, MutableList<RegistryEntry<T>>>?) {
        }

        override fun getOrCreateEntryList(tag: TagKey<T>?): RegistryEntryList.Named<T> {
            return getEntryList(tag).orElseGet { RegistryEntryList.of(registryWrapper, tag) }
        }

        override fun getEntryList(tag: TagKey<T>?): Optional<RegistryEntryList.Named<T>> {
            return registryWrapper.getOptional(tag ?: return Optional.empty())
        }

        override fun getEntry(value: T): RegistryEntry<T> {
            return registryWrapper.getOptional(inverseLookup[value]!!).get()
        }

        override fun getEntry(key: RegistryKey<T>?): Optional<RegistryEntry.Reference<T>> {
            return registryWrapper.getOptional(key ?: return Optional.empty())
        }

        override fun createEntry(value: T): RegistryEntry.Reference<T> {
            TODO()
        }

        override fun contains(key: RegistryKey<T>?): Boolean {
            return getEntry(key).isPresent
        }

        override fun getEntryLifecycle(entry: T): Lifecycle {
            return Lifecycle.stable()
        }

        override fun getId(value: T): Identifier? {
            return (inverseLookup[value] ?: return null).value
        }

        override fun getKey(entry: T): Optional<RegistryKey<T>> {
            return Optional.ofNullable(inverseLookup[entry ?: return Optional.empty()])
        }
    }
}

fun createDynamicRegistry(): DynamicRegistryManager.Immutable {
    val wrapperLookup = BuiltinRegistries.createWrapperLookup()
    return object : DynamicRegistryManager.Immutable {
        override fun <E : Any?> getOptional(key: RegistryKey<out Registry<out E>>): Optional<Registry<E>> {
            val lookup = wrapperLookup.getOptionalWrapper(key).getOrNull() ?: return Optional.empty()
            val registry = makeRegistry(lookup, key as RegistryKey<out Registry<E>>)
            return Optional.of(registry)
        }

        fun <T> entry(reg: RegistryKey<out Registry<T>>): DynamicRegistryManager.Entry<T> {
            return DynamicRegistryManager.Entry(reg, getOptional(reg).get())
        }

        override fun streamAllRegistries(): Stream<DynamicRegistryManager.Entry<*>> {
            return wrapperLookup.streamAllRegistryKeys()
                .map { entry(it as RegistryKey<out Registry<Any>>) }
        }
    }
}

class FakeWorld(registries: DynamicRegistryManager.Immutable = createDynamicRegistry()) : World(
    Properties,
    RegistryKey.of(RegistryKeys.WORLD, Identifier.of("firmament", "fakeworld")),
    registries,
    registries[RegistryKeys.DIMENSION_TYPE].entryOf(
        RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE,
            Identifier("minecraft", "overworld")
        )
    ),
    { DummyProfiler.INSTANCE },
    true,
    false,
    0, 0
) {
    object Properties : MutableWorldProperties {
        override fun getSpawnX(): Int {
            return 0
        }

        override fun getSpawnY(): Int {
            return 0
        }

        override fun getSpawnZ(): Int {
            return 0
        }

        override fun getSpawnAngle(): Float {
            return 0F
        }

        override fun getTime(): Long {
            return 0
        }

        override fun getTimeOfDay(): Long {
            return 0
        }

        override fun isThundering(): Boolean {
            return false
        }

        override fun isRaining(): Boolean {
            return false
        }

        override fun setRaining(raining: Boolean) {
        }

        override fun isHardcore(): Boolean {
            return false
        }

        override fun getGameRules(): GameRules {
            return GameRules()
        }

        override fun getDifficulty(): Difficulty {
            return Difficulty.HARD
        }

        override fun isDifficultyLocked(): Boolean {
            return false
        }

        override fun setSpawnX(spawnX: Int) {
        }

        override fun setSpawnY(spawnY: Int) {
        }

        override fun setSpawnZ(spawnZ: Int) {
        }

        override fun setSpawnAngle(spawnAngle: Float) {
        }
    }

    override fun getPlayers(): List<PlayerEntity> {
        return emptyList()
    }

    override fun getBrightness(direction: Direction?, shaded: Boolean): Float {
        return 1f
    }

    override fun getGeneratorStoredBiome(biomeX: Int, biomeY: Int, biomeZ: Int): RegistryEntry<Biome> {
        return registryManager.get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS)
    }

    override fun getEnabledFeatures(): FeatureSet {
        return FeatureFlags.VANILLA_FEATURES
    }

    class FakeTickScheduler<T> : QueryableTickScheduler<T> {
        override fun scheduleTick(orderedTick: OrderedTick<T>?) {
        }

        override fun isQueued(pos: BlockPos?, type: T): Boolean {
            return true
        }

        override fun getTickCount(): Int {
            return 0
        }

        override fun isTicking(pos: BlockPos?, type: T): Boolean {
            return true
        }

    }

    override fun getBlockTickScheduler(): QueryableTickScheduler<Block> {
        return FakeTickScheduler()
    }

    override fun getFluidTickScheduler(): QueryableTickScheduler<Fluid> {
        return FakeTickScheduler()
    }


    class FakeChunkManager(val world: FakeWorld) : ChunkManager() {
        override fun getChunk(x: Int, z: Int, leastStatus: ChunkStatus?, create: Boolean): Chunk {
            return EmptyChunk(world, ChunkPos(x,z), world.registryManager.get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS))
        }

        override fun getWorld(): BlockView {
            return world
        }

        override fun tick(shouldKeepTicking: BooleanSupplier?, tickChunks: Boolean) {
        }

        override fun getDebugString(): String {
            return "FakeChunkManager"
        }

        override fun getLoadedChunkCount(): Int {
            return 0
        }

        override fun getLightingProvider(): LightingProvider {
            return FakeLightingProvider(this)
        }
    }

    class FakeLightingProvider(chunkManager: FakeChunkManager) : LightingProvider(chunkManager, false, false)

    override fun getChunkManager(): ChunkManager {
        return FakeChunkManager(this)
    }

    override fun playSound(
        source: PlayerEntity?,
        x: Double,
        y: Double,
        z: Double,
        sound: RegistryEntry<SoundEvent>?,
        category: SoundCategory?,
        volume: Float,
        pitch: Float,
        seed: Long
    ) {
    }

    override fun syncWorldEvent(player: PlayerEntity?, eventId: Int, pos: BlockPos?, data: Int) {
    }

    override fun emitGameEvent(event: GameEvent?, emitterPos: Vec3d?, emitter: GameEvent.Emitter?) {
    }

    override fun updateListeners(pos: BlockPos?, oldState: BlockState?, newState: BlockState?, flags: Int) {
    }

    override fun playSoundFromEntity(
        source: PlayerEntity?,
        entity: Entity?,
        sound: RegistryEntry<SoundEvent>?,
        category: SoundCategory?,
        volume: Float,
        pitch: Float,
        seed: Long
    ) {
    }

    override fun asString(): String {
        return "FakeWorld"
    }

    override fun getEntityById(id: Int): Entity? {
        return null
    }

    override fun getTickManager(): TickManager {
        return TickManager()
    }

    override fun getMapState(id: String?): MapState? {
        return null
    }

    override fun putMapState(id: String?, state: MapState?) {
    }

    override fun getNextMapId(): Int {
        return 0
    }

    override fun setBlockBreakingInfo(entityId: Int, pos: BlockPos?, progress: Int) {
    }

    override fun getScoreboard(): Scoreboard {
        return Scoreboard()
    }

    override fun getRecipeManager(): RecipeManager {
        return RecipeManager()
    }

    object FakeEntityLookup : EntityLookup<Entity> {
        override fun get(id: Int): Entity? {
            return null
        }

        override fun get(uuid: UUID?): Entity? {
            return null
        }

        override fun iterate(): MutableIterable<Entity> {
            return mutableListOf()
        }

        override fun <U : Entity?> forEachIntersects(
            filter: TypeFilter<Entity, U>?,
            box: Box?,
            consumer: LazyIterationConsumer<U>?
        ) {
        }

        override fun forEachIntersects(box: Box?, action: Consumer<Entity>?) {
        }

        override fun <U : Entity?> forEach(filter: TypeFilter<Entity, U>?, consumer: LazyIterationConsumer<U>?) {
        }

    }

    override fun getEntityLookup(): EntityLookup<Entity> {
        return FakeEntityLookup
    }
}

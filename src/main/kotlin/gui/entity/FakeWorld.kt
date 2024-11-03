package moe.nea.firmament.gui.entity

import java.util.UUID
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.component.type.MapIdComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.item.FuelRegistry
import net.minecraft.item.map.MapState
import net.minecraft.particle.ParticleEffect
import net.minecraft.recipe.BrewingRecipeRegistry
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipePropertySet
import net.minecraft.recipe.StonecuttingRecipe
import net.minecraft.recipe.display.CuttingRecipeDisplay
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.ServerDynamicRegistryType
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.resource.DataConfiguration
import net.minecraft.resource.ResourcePackManager
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.server.SaveLoading
import net.minecraft.server.command.CommandManager
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
import net.minecraft.world.BlockView
import net.minecraft.world.Difficulty
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
import net.minecraft.world.explosion.ExplosionBehavior
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.QueryableTickScheduler
import net.minecraft.world.tick.TickManager
import moe.nea.firmament.util.MC

fun createDynamicRegistry(): DynamicRegistryManager.Immutable {
	// TODO: use SaveLoading.load() to properly load a full registry
	return DynamicRegistryManager.of(Registries.REGISTRIES)
}

class FakeWorld(
	registries: DynamicRegistryManager.Immutable = createDynamicRegistry(),
) : World(
	Properties,
	RegistryKey.of(RegistryKeys.WORLD, Identifier.of("firmament", "fakeworld")),
	registries,
	MC.defaultRegistries.getOrThrow(RegistryKeys.DIMENSION_TYPE)
		.getOrThrow(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of("minecraft", "overworld"))),
	true,
	false,
	0L,
	0
) {
	object Properties : MutableWorldProperties {
		override fun getSpawnPos(): BlockPos {
			return BlockPos.ORIGIN
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

		override fun getDifficulty(): Difficulty {
			return Difficulty.HARD
		}

		override fun isDifficultyLocked(): Boolean {
			return false
		}

		override fun setSpawnPos(pos: BlockPos?, angle: Float) {}
	}

	override fun getPlayers(): List<PlayerEntity> {
		return emptyList()
	}

	override fun getBrightness(direction: Direction?, shaded: Boolean): Float {
		return 1f
	}

	override fun getGeneratorStoredBiome(biomeX: Int, biomeY: Int, biomeZ: Int): RegistryEntry<Biome> {
		return registryManager.getOptionalEntry(BiomeKeys.PLAINS).get()
	}

	override fun getSeaLevel(): Int {
		return 0
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
			return EmptyChunk(
				world,
				ChunkPos(x, z),
				world.registryManager.getOptionalEntry(BiomeKeys.PLAINS).get()
			)
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

	override fun emitGameEvent(event: RegistryEntry<GameEvent>?, emitterPos: Vec3d?, emitter: GameEvent.Emitter?) {
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

	override fun createExplosion(
		entity: Entity?,
		damageSource: DamageSource?,
		behavior: ExplosionBehavior?,
		x: Double,
		y: Double,
		z: Double,
		power: Float,
		createFire: Boolean,
		explosionSourceType: ExplosionSourceType?,
		smallParticle: ParticleEffect?,
		largeParticle: ParticleEffect?,
		soundEvent: RegistryEntry<SoundEvent>?
	) {
		TODO("Not yet implemented")
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

	override fun getMapState(id: MapIdComponent?): MapState? {
		return null
	}

	override fun putMapState(id: MapIdComponent?, state: MapState?) {
	}

	override fun increaseAndGetMapId(): MapIdComponent {
		return MapIdComponent(0)
	}

	override fun setBlockBreakingInfo(entityId: Int, pos: BlockPos?, progress: Int) {
	}

	override fun getScoreboard(): Scoreboard {
		return Scoreboard()
	}

	override fun getRecipeManager(): RecipeManager {
		return object : RecipeManager {
			override fun getPropertySet(key: RegistryKey<RecipePropertySet>?): RecipePropertySet {
				return RecipePropertySet.EMPTY
			}

			override fun getStonecutterRecipes(): CuttingRecipeDisplay.Grouping<StonecuttingRecipe> {
				return CuttingRecipeDisplay.Grouping.empty()
			}
		}
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

	override fun getBrewingRecipeRegistry(): BrewingRecipeRegistry {
		return BrewingRecipeRegistry.EMPTY
	}

	override fun getFuelRegistry(): FuelRegistry {
		TODO("Not yet implemented")
	}
}

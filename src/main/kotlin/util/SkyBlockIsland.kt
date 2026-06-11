package moe.nea.firmament.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.repo.RepoManager

@Serializable(with = SkyBlockIsland.Serializer::class)
class SkyBlockIsland
private constructor(
	val locrawMode: String,
) {

	object Serializer : KSerializer<SkyBlockIsland> {
		override val descriptor: SerialDescriptor
			get() = PrimitiveSerialDescriptor("SkyBlockIsland", PrimitiveKind.STRING)

		override fun deserialize(decoder: Decoder): SkyBlockIsland {
			return forMode(decoder.decodeString())
		}

		override fun serialize(encoder: Encoder, value: SkyBlockIsland) {
			encoder.encodeString(value.locrawMode)
		}
	}

	companion object {
		private val allIslands = mutableMapOf<String, SkyBlockIsland>()
		fun forMode(mode: String): SkyBlockIsland = allIslands.computeIfAbsent(mode, ::SkyBlockIsland)
		val HUB = forMode("hub")
		val RIFT = forMode("rift")
		val PRIVATE_ISLAND = forMode("dynamic")
		val DUNGEON_HUB = forMode("dungeon_hub")
		val JERRY_WORKSHOP = forMode("winter")

		// Instanced
		val DUNGEON = forMode("dungeon")
		val KUUDRA = forMode("kuudra")

		// Mining
		val GOLD_MINE = forMode("mining_1")
		val DEEP_CAVERNS = forMode("mining_2")
		val DWARVEN_MINES = forMode("mining_3")
		val CRYSTAL_HOLLOWS = forMode("crystal_hollows")
		val MINESHAFT = forMode("mineshaft")

		// Combat
		val SPIDER = forMode("combat_1")
		val END = forMode("combat_3")
		val CRIMSON_ISLE = forMode("crimson_isle")

		// Farming
		val FARMING_ISLANDS = forMode("farming_1") // The Barn & Mushroom Desert
		val GARDEN = forMode("garden")

		// Foraging
		val PARK = forMode("foraging_1")
		val GALATEA = forMode("foraging_2")

		// Fishing
		val BACKWATER_BAYOU = forMode("fishing_1")

		val NIL = forMode("_")
	}

	val hasCustomMining
		get() = RepoManager.miningData.customMiningAreas[this]?.isSpecialMining ?: false

	val userFriendlyName
		get() = RepoManager.neuRepo.constants.islands.areaNames
			.getOrDefault(locrawMode, locrawMode)
}

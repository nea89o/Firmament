package moe.nea.firmament.repo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblock.Rarity

@Serializable
data class Reforge(
	val reforgeName: String,
	@SerialName("internalName") val reforgeStone: SkyblockId? = null,
	val nbtModifier: ReforgeId? = null,
	val requiredRarities: List<Rarity>? = null,
	val itemTypes: @Serializable(with = ReforgeEligibilityFilter.ItemTypesSerializer::class) List<ReforgeEligibilityFilter>? = null,
	val allowOn: List<ReforgeEligibilityFilter>? = null,
	val reforgeCosts: RarityMapped<Double>? = null,
	val reforgeAbility: RarityMapped<String>? = null,
	val reforgeStats: RarityMapped<Map<String, Double>>? = null,
) {
	val eligibleItems get() = allowOn ?: itemTypes ?: listOf()

	@Serializable(with = ReforgeEligibilityFilter.Serializer::class)
	sealed interface ReforgeEligibilityFilter {
		object ItemTypesSerializer : KSerializer<List<ReforgeEligibilityFilter>> {
			override val descriptor: SerialDescriptor
				get() = JsonElement.serializer().descriptor

			override fun deserialize(decoder: Decoder): List<ReforgeEligibilityFilter> {
				decoder as JsonDecoder
				val jsonElement = decoder.decodeJsonElement()
				if (jsonElement is JsonPrimitive && jsonElement.isString) {
					return jsonElement.content.split("/").map { AllowsItemType(ItemType.ofName(it)) }
				}
				if (jsonElement is JsonArray) {
					return decoder.json.decodeFromJsonElement(serializer<List<ReforgeEligibilityFilter>>(), jsonElement)
				}
				jsonElement as JsonObject
				val filters = mutableListOf<ReforgeEligibilityFilter>()
				jsonElement["internalName"]?.let {
					decoder.json.decodeFromJsonElement(serializer<List<SkyblockId>>(), it).forEach {
						filters.add(AllowsInternalName(it))
					}
				}
				jsonElement["itemId"]?.let {
					decoder.json.decodeFromJsonElement(serializer<List<String>>(), it).forEach {
						val ident = Identifier.tryParse(it)
						if (ident != null)
							filters.add(AllowsVanillaItemType(RegistryKey.of(RegistryKeys.ITEM, ident)))
					}
				}
				return filters
			}

			override fun serialize(encoder: Encoder, value: List<ReforgeEligibilityFilter>) {
				TODO("Not yet implemented")
			}
		}

		object Serializer : KSerializer<ReforgeEligibilityFilter> {
			override val descriptor: SerialDescriptor
				get() = serializer<JsonElement>().descriptor

			override fun deserialize(decoder: Decoder): ReforgeEligibilityFilter {
				val jsonObject = serializer<JsonObject>().deserialize(decoder)
				jsonObject["internalName"]?.let {
					return AllowsInternalName(SkyblockId((it as JsonPrimitive).content))
				}
				jsonObject["itemType"]?.let {
					return AllowsItemType(ItemType.ofName((it as JsonPrimitive).content))
				}
				jsonObject["minecraftId"]?.let {
					return AllowsVanillaItemType(RegistryKey.of(RegistryKeys.ITEM,
					                                            Identifier.of((it as JsonPrimitive).content)))
				}
				error("Unknown item type")
			}

			override fun serialize(encoder: Encoder, value: ReforgeEligibilityFilter) {
				TODO("Not yet implemented")
			}

		}

		data class AllowsItemType(val itemType: ItemType) : ReforgeEligibilityFilter
		data class AllowsInternalName(val internalName: SkyblockId) : ReforgeEligibilityFilter
		data class AllowsVanillaItemType(val minecraftId: RegistryKey<Item>) : ReforgeEligibilityFilter
	}


	val reforgeId get() = nbtModifier ?: ReforgeId(reforgeName.lowercase())

	@Serializable(with = RarityMapped.Serializer::class)
	sealed interface RarityMapped<T> {
		class Serializer<T>(
			val values: KSerializer<T>
		) : KSerializer<RarityMapped<T>> {
			override val descriptor: SerialDescriptor
				get() = JsonElement.serializer().descriptor

			val indirect = MapSerializer(Rarity.serializer(), values)
			override fun deserialize(decoder: Decoder): RarityMapped<T> {
				decoder as JsonDecoder
				val element = decoder.decodeJsonElement()
				if (element is JsonObject) {
					return PerRarity(decoder.json.decodeFromJsonElement(indirect, element))
				} else {
					return Direct(decoder.json.decodeFromJsonElement(values, element))
				}
			}

			override fun serialize(encoder: Encoder, value: RarityMapped<T>) {
				when (value) {
					is Direct<T> ->
						values.serialize(encoder, value.value)

					is PerRarity<T> ->
						indirect.serialize(encoder, value.values)
				}
			}
		}

		@Serializable
		data class Direct<T>(val value: T) : RarityMapped<T>

		@Serializable
		data class PerRarity<T>(val values: Map<Rarity, T>) : RarityMapped<T>
	}

}

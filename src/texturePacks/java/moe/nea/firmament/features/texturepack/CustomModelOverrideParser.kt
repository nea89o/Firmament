package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Encoder
import net.minecraft.client.render.item.model.ItemModelTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.features.texturepack.predicates.AndPredicate
import moe.nea.firmament.features.texturepack.predicates.CastPredicate
import moe.nea.firmament.features.texturepack.predicates.DisplayNamePredicate
import moe.nea.firmament.features.texturepack.predicates.ExtraAttributesPredicate
import moe.nea.firmament.features.texturepack.predicates.ItemPredicate
import moe.nea.firmament.features.texturepack.predicates.LorePredicate
import moe.nea.firmament.features.texturepack.predicates.NotPredicate
import moe.nea.firmament.features.texturepack.predicates.OrPredicate
import moe.nea.firmament.features.texturepack.predicates.PetPredicate
import moe.nea.firmament.util.json.KJsonOps

object CustomModelOverrideParser {

	val LEGACY_CODEC: Codec<FirmamentModelPredicate> =
		Codec.of(
			Encoder.error("cannot encode legacy firmament model predicates"),
			object : Decoder<FirmamentModelPredicate> {
				override fun <T : Any?> decode(
					ops: DynamicOps<T>,
					input: T
				): DataResult<Pair<FirmamentModelPredicate, T>> {
					try {
						val pred = Firmament.json.decodeFromJsonElement(
							FirmamentRootPredicateSerializer,
							ops.convertTo(KJsonOps.INSTANCE, input))
						return DataResult.success(Pair.of(pred, ops.empty()))
					} catch (ex: Exception) {
						return DataResult.error { "Could not deserialize ${ex.message}" }
					}
				}
			}
		)

	val predicateParsers = mutableMapOf<Identifier, FirmamentModelPredicateParser>()


	fun registerPredicateParser(name: String, parser: FirmamentModelPredicateParser) {
		predicateParsers[Identifier.of("firmament", name)] = parser
	}

	init {
		registerPredicateParser("display_name", DisplayNamePredicate.Parser)
		registerPredicateParser("lore", LorePredicate.Parser)
		registerPredicateParser("all", AndPredicate.Parser)
		registerPredicateParser("any", OrPredicate.Parser)
		registerPredicateParser("not", NotPredicate.Parser)
		registerPredicateParser("item", ItemPredicate.Parser)
		registerPredicateParser("extra_attributes", ExtraAttributesPredicate.Parser)
		registerPredicateParser("pet", PetPredicate.Parser)
	}

	private val neverPredicate = listOf(
		object : FirmamentModelPredicate {
			override fun test(stack: ItemStack): Boolean {
				return false
			}
		}
	)

	fun parsePredicates(predicates: JsonObject?): List<FirmamentModelPredicate> {
		if (predicates == null) return neverPredicate
		val parsedPredicates = mutableListOf<FirmamentModelPredicate>()
		for (predicateName in predicates.keySet()) {
			if (predicateName == "cast") { // 1.21.4
				parsedPredicates.add(CastPredicate.Parser.parse(predicates[predicateName]) ?: return neverPredicate)
			}
			if (!predicateName.startsWith("firmament:")) continue
			val identifier = Identifier.of(predicateName)
			val parser = predicateParsers[identifier] ?: return neverPredicate
			val parsedPredicate = parser.parse(predicates[predicateName]) ?: return neverPredicate
			parsedPredicates.add(parsedPredicate)
		}
		return parsedPredicates
	}

	@JvmStatic
	fun parseCustomModelOverrides(jsonObject: JsonObject): Array<FirmamentModelPredicate>? {
		val predicates = (jsonObject["predicate"] as? JsonObject) ?: return null
		val parsedPredicates = parsePredicates(predicates)
		if (parsedPredicates.isEmpty())
			return null
		return parsedPredicates.toTypedArray()
	}

	@Subscribe
	fun finalizeResources(event: FinalizeResourceManagerEvent) {
		ItemModelTypes.ID_MAPPER.put(
			Firmament.identifier("predicates/legacy"),
			PredicateModel.Unbaked.CODEC
		)
	}

}

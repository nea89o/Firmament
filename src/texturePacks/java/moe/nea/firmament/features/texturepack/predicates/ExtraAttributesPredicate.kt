
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import com.mojang.serialization.JsonOps
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.features.texturepack.StringMatcher
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString
import moe.nea.firmament.util.Base64Util
import moe.nea.firmament.util.extraAttributes

fun interface NbtMatcher {
	fun matches(nbt: NbtElement): Boolean

	object Parser {
		fun parse(jsonElement: JsonElement): NbtMatcher? {
			if (jsonElement is JsonPrimitive) {
				if (jsonElement.isString) {
					val string = jsonElement.asString
					return MatchStringExact(string)
				}
				if (jsonElement.isNumber) {
					return MatchNumberExact(jsonElement.asLong) // TODO: parse generic number
				}
			}
			if (jsonElement is JsonObject) {
				var encounteredParser: NbtMatcher? = null
				for (entry in ExclusiveParserType.entries) {
					val data = jsonElement[entry.key] ?: continue
					if (encounteredParser != null) {
						// TODO: warn
						return null
					}
					encounteredParser = entry.parse(data) ?: return null
				}
				return encounteredParser
			}
			return null
		}

		enum class ExclusiveParserType(val key: String) {
			STRING("string") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return MatchString(StringMatcher.parse(element))
				}
			},
			INT("int") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asInt },
						{ (it as? NbtInt)?.intValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			FLOAT("float") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asFloat },
						{ (it as? NbtFloat)?.floatValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			DOUBLE("double") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asDouble },
						{ (it as? NbtDouble)?.doubleValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			LONG("long") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asLong },
						{ (it as? NbtLong)?.longValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			SHORT("short") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asShort },
						{ (it as? NbtShort)?.shortValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			BYTE("byte") {
				override fun parse(element: JsonElement): NbtMatcher? {
					return parseGenericNumber(
						element,
						{ it.asByte },
						{ (it as? NbtByte)?.byteValue() },
						{ a, b ->
							if (a == b) Comparison.EQUAL
							else if (a < b) Comparison.LESS_THAN
							else Comparison.GREATER
						})
				}
			},
			;

			abstract fun parse(element: JsonElement): NbtMatcher?
		}

		enum class Comparison {
			LESS_THAN, EQUAL, GREATER
		}

		inline fun <T : Any> parseGenericNumber(
			jsonElement: JsonElement,
			primitiveExtractor: (JsonPrimitive) -> T?,
			crossinline nbtExtractor: (NbtElement) -> T?,
			crossinline compare: (T, T) -> Comparison
		): NbtMatcher? {
			if (jsonElement is JsonPrimitive) {
				val expected = primitiveExtractor(jsonElement) ?: return null
				return NbtMatcher {
					val actual = nbtExtractor(it) ?: return@NbtMatcher false
					compare(actual, expected) == Comparison.EQUAL
				}
			}
			if (jsonElement is JsonObject) {
				val minElement = jsonElement.getAsJsonPrimitive("min")
				val min = if (minElement != null) primitiveExtractor(minElement) ?: return null else null
				val minExclusive = jsonElement.get("minExclusive")?.asBoolean ?: false
				val maxElement = jsonElement.getAsJsonPrimitive("max")
				val max = if (maxElement != null) primitiveExtractor(maxElement) ?: return null else null
				val maxExclusive = jsonElement.get("maxExclusive")?.asBoolean ?: true
				if (min == null && max == null) return null
				return NbtMatcher {
					val actual = nbtExtractor(it) ?: return@NbtMatcher false
					if (max != null) {
						val comp = compare(actual, max)
						if (comp == Comparison.GREATER) return@NbtMatcher false
						if (comp == Comparison.EQUAL && maxExclusive) return@NbtMatcher false
					}
					if (min != null) {
						val comp = compare(actual, min)
						if (comp == Comparison.LESS_THAN) return@NbtMatcher false
						if (comp == Comparison.EQUAL && minExclusive) return@NbtMatcher false
					}
					return@NbtMatcher true
				}
			}
			return null

		}
	}

	class MatchNumberExact(val number: Long) : NbtMatcher {
		override fun matches(nbt: NbtElement): Boolean {
			return when (nbt) {
				is NbtByte -> nbt.byteValue().toLong() == number
				is NbtInt -> nbt.intValue().toLong() == number
				is NbtShort -> nbt.shortValue().toLong() == number
				is NbtLong -> nbt.longValue().toLong() == number
				else -> false
			}
		}

	}

	class MatchStringExact(val string: String) : NbtMatcher {
		override fun matches(nbt: NbtElement): Boolean {
			return nbt.asString() == string
		}

		override fun toString(): String {
			return "MatchNbtStringExactly($string)"
		}
	}

	class MatchString(val string: StringMatcher) : NbtMatcher {
		override fun matches(nbt: NbtElement): Boolean {
			return nbt.asString().let(string::matches)
		}

		override fun toString(): String {
			return "MatchNbtString($string)"
		}
	}
}

data class ExtraAttributesPredicate(
	val path: NbtPrism,
	val matcher: NbtMatcher,
) : FirmamentModelPredicate {

	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
			if (jsonElement !is JsonObject) return null
			val path = jsonElement.get("path") ?: return null
			val prism = NbtPrism.fromElement(path) ?: return null
			val matcher = NbtMatcher.Parser.parse(jsonElement.get("match") ?: jsonElement)
				?: return null
			return ExtraAttributesPredicate(prism, matcher)
		}
	}

	override fun test(stack: ItemStack): Boolean {
		return path.access(stack.extraAttributes)
			.any { matcher.matches(it) }
	}
}

class NbtPrism(val path: List<String>) {
	companion object {
		fun fromElement(path: JsonElement): NbtPrism? {
			if (path is JsonArray) {
				return NbtPrism(path.map { (it as JsonPrimitive).asString })
			} else if (path is JsonPrimitive && path.isString) {
				return NbtPrism(path.asString.split("."))
			}
			return null
		}
	}

	override fun toString(): String {
		return "Prism($path)"
	}

	fun access(root: NbtElement): Collection<NbtElement> {
		var rootSet = mutableListOf(root)
		var switch = mutableListOf<NbtElement>()
		for (pathSegment in path) {
			if (pathSegment == ".") continue
			if (pathSegment != "*" && pathSegment.startsWith("*")) {
				if (pathSegment == "*json") {
					for (element in rootSet) {
						val eString = element.asString() ?: continue
						val element = Gson().fromJson(eString, JsonElement::class.java)
						switch.add(JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element))
					}
				} else if (pathSegment == "*base64") {
					for (element in rootSet) {
						val string = element.asString() ?: continue
						switch.add(NbtString.of(Base64Util.decodeString(string)))
					}
				}
			}
			for (element in rootSet) {
				if (element is NbtList) {
					if (pathSegment == "*")
						switch.addAll(element)
					val index = pathSegment.toIntOrNull() ?: continue
					if (index !in element.indices) continue
					switch.add(element[index])
				}
				if (element is NbtCompound) {
					if (pathSegment == "*")
						element.keys.mapTo(switch) { element.get(it)!! }
					switch.add(element.get(pathSegment) ?: continue)
				}
			}
			val temp = switch
			switch = rootSet
			rootSet = temp
			switch.clear()
		}
		return rootSet
	}
}

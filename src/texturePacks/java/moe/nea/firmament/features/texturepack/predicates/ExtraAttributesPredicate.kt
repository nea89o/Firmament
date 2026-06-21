
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.features.texturepack.StringMatcher
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.ShortTag
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.mc.NbtPrism
import moe.nea.firmament.util.mc.accessor

fun interface NbtMatcher {
	fun matches(nbt: Tag): Boolean

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
						{ (it as? IntTag)?.intValue() },
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
						{ (it as? FloatTag)?.floatValue() },
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
						{ (it as? DoubleTag)?.doubleValue() },
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
						{ (it as? LongTag)?.longValue() },
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
						{ (it as? ShortTag)?.shortValue() },
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
						{ (it as? ByteTag)?.byteValue() },
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
            crossinline nbtExtractor: (Tag) -> T?,
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
		override fun matches(nbt: Tag): Boolean {
			return when (nbt) {
				is ByteTag -> nbt.byteValue().toLong() == number
				is IntTag -> nbt.intValue().toLong() == number
				is ShortTag -> nbt.shortValue().toLong() == number
				is LongTag -> nbt.longValue().toLong() == number
				else -> false
			}
		}

	}

	class MatchStringExact(val string: String) : NbtMatcher {
		override fun matches(nbt: Tag): Boolean {
			return nbt.asString().getOrNull() == string
		}

		override fun toString(): String {
			return "MatchNbtStringExactly($string)"
		}
	}

	class MatchString(val string: StringMatcher) : NbtMatcher {
		override fun matches(nbt: Tag): Boolean {
			return nbt.asString().map(string::matches).getOrDefault(false)
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
		return path.access(stack.accessor().extraAttributes)
			.any { matcher.matches(it) }
	}
}


package moe.nea.firmament.util.json

import com.google.gson.internal.LazilyParsedNumber
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import java.util.stream.Stream
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlin.streams.asSequence

class KJsonOps : DynamicOps<JsonElement> {
	companion object {
		val INSTANCE = KJsonOps()
	}

	override fun empty(): JsonElement {
		return JsonNull
	}

	override fun createNumeric(num: Number): JsonElement {
		return JsonPrimitive(num)
	}

	override fun createString(str: String): JsonElement {
		return JsonPrimitive(str)
	}

	override fun remove(input: JsonElement, key: String): JsonElement {
		if (input is JsonObject) {
			return JsonObject(input.filter { it.key != key })
		} else {
			return input
		}
	}

	override fun createList(stream: Stream<JsonElement>): JsonElement {
		return JsonArray(stream.toList())
	}

	override fun getStream(input: JsonElement): DataResult<Stream<JsonElement>> {
		if (input is JsonArray)
			return DataResult.success(input.stream())
		return DataResult.error { "Not a json array: $input" }
	}

	override fun createMap(map: Stream<Pair<JsonElement, JsonElement>>): JsonElement {
		return JsonObject(map.asSequence()
			                  .map { ((it.first as JsonPrimitive).content) to it.second }
			                  .toMap())
	}

	override fun getMapValues(input: JsonElement): DataResult<Stream<Pair<JsonElement, JsonElement>>> {
		if (input is JsonObject) {
			return DataResult.success(input.entries.stream().map { Pair.of(createString(it.key), it.value) })
		}
		return DataResult.error { "Not a JSON object: $input" }
	}

	override fun mergeToMap(map: JsonElement, key: JsonElement, value: JsonElement): DataResult<JsonElement> {
		if (key !is JsonPrimitive || key.isString) {
			return DataResult.error { "key is not a string: $key" }
		}
		val jKey = key.content
		val extra = mapOf(jKey to value)
		if (map == empty()) {
			return DataResult.success(JsonObject(extra))
		}
		if (map is JsonObject) {
			return DataResult.success(JsonObject(map + extra))
		}
		return DataResult.error { "mergeToMap called with not a map: $map" }
	}

	override fun mergeToList(list: JsonElement, value: JsonElement): DataResult<JsonElement> {
		if (list == empty())
			return DataResult.success(JsonArray(listOf(value)))
		if (list is JsonArray) {
			return DataResult.success(JsonArray(list + value))
		}
		return DataResult.error { "mergeToList called with not a list: $list" }
	}

	override fun getStringValue(input: JsonElement): DataResult<String> {
		if (input is JsonPrimitive && input.isString) {
			return DataResult.success(input.content)
		}
		return DataResult.error { "Not a string: $input" }
	}

	override fun getNumberValue(input: JsonElement): DataResult<Number> {
		if (input is JsonPrimitive && !input.isString && input.booleanOrNull == null)
			return DataResult.success(LazilyParsedNumber(input.content))
		return DataResult.error { "not a number: $input" }
	}

	override fun createBoolean(value: Boolean): JsonElement {
		return JsonPrimitive(value)
	}

	override fun getBooleanValue(input: JsonElement): DataResult<Boolean> {
		if (input is JsonPrimitive) {
			if (input.booleanOrNull != null)
				return DataResult.success(input.boolean)
			return super.getBooleanValue(input)
		}
		return DataResult.error { "Not a boolean: $input" }
	}

	override fun <U : Any?> convertTo(output: DynamicOps<U>, input: JsonElement): U {
		if (input is JsonObject)
			return output.createMap(
				input.entries.stream().map { Pair.of(output.createString(it.key), convertTo(output, it.value)) })
		if (input is JsonArray)
			return output.createList(input.stream().map { convertTo(output, it) })
		if (input is JsonNull)
			return output.empty()
		if (input is JsonPrimitive) {
			if (input.isString)
				return output.createString(input.content)
			if (input.booleanOrNull != null)
				return output.createBoolean(input.boolean)
		}
		error("Unknown json value: $input")
	}
}

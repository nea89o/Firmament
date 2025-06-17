package moe.nea.firmament.util.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun <T : JsonElement> List<T>.asJsonArray(): JsonArray {
	return JsonArray(this)
}

fun Iterable<String>.toJsonArray(): JsonArray = map { JsonPrimitive(it) }.asJsonArray()

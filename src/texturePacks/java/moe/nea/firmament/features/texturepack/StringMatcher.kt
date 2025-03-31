
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber
import java.util.function.Predicate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.optionals.getOrNull
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.removeColorCodes

@Serializable(with = StringMatcher.Serializer::class)
interface StringMatcher {
    fun matches(string: String): Boolean
    fun matches(text: Text): Boolean {
        return matches(text.string)
    }

    fun matches(nbt: NbtString): Boolean {
        val string = nbt.value
        val jsonStart = string.indexOf('{')
        val stringStart = string.indexOf('"')
        val isString = stringStart >= 0 && string.subSequence(0, stringStart).isBlank()
        val isJson = jsonStart >= 0 && string.subSequence(0, jsonStart).isBlank()
        if (isString || isJson)
            return matches(Text.Serialization.fromJson(string, MC.defaultRegistries) ?: return false)
        return matches(string)
    }

    class Equals(input: String, val stripColorCodes: Boolean) : StringMatcher {
        private val expected = if (stripColorCodes) input.removeColorCodes() else input
        override fun matches(string: String): Boolean {
            return expected == (if (stripColorCodes) string.removeColorCodes() else string)
        }

        override fun toString(): String {
            return "Equals($expected, stripColorCodes = $stripColorCodes)"
        }
    }

    class Pattern(val patternWithColorCodes: String, val stripColorCodes: Boolean) : StringMatcher {
        private val regex: Predicate<String> = patternWithColorCodes.toPattern().asMatchPredicate()
        override fun matches(string: String): Boolean {
            return regex.test(if (stripColorCodes) string.removeColorCodes() else string)
        }

        override fun toString(): String {
            return "Pattern($patternWithColorCodes, stripColorCodes = $stripColorCodes)"
        }
    }

    object Serializer : KSerializer<StringMatcher> {
        val delegateSerializer = kotlinx.serialization.json.JsonElement.serializer()
        override val descriptor: SerialDescriptor
            get() = SerialDescriptor("StringMatcher", delegateSerializer.descriptor)

        override fun deserialize(decoder: Decoder): StringMatcher {
            val delegate = decoder.decodeSerializableValue(delegateSerializer)
            val gsonDelegate = delegate.intoGson()
            return parse(gsonDelegate)
        }

        override fun serialize(encoder: Encoder, value: StringMatcher) {
            encoder.encodeSerializableValue(delegateSerializer, serialize(value).intoKotlinJson())
        }

    }

    companion object {
        fun serialize(stringMatcher: StringMatcher): JsonElement {
            TODO("Cannot serialize string matchers rn")
        }

        fun parse(jsonElement: JsonElement): StringMatcher {
            if (jsonElement is JsonPrimitive) {
                return Equals(jsonElement.asString, true)
            }
            if (jsonElement is JsonObject) {
                val regex = jsonElement["regex"] as JsonPrimitive?
                val text = jsonElement["equals"] as JsonPrimitive?
                val shouldStripColor = when (val color = (jsonElement["color"] as JsonPrimitive?)?.asString) {
                    "preserve" -> false
                    "strip", null -> true
                    else -> error("Unknown color preservation mode: $color")
                }
                if ((regex == null) == (text == null)) error("Could not parse $jsonElement as string matcher")
                if (regex != null)
                    return Pattern(regex.asString, shouldStripColor)
                if (text != null)
                    return Equals(text.asString, shouldStripColor)
            }
            error("Could not parse $jsonElement as a string matcher")
        }
    }
}

fun JsonElement.intoKotlinJson(): kotlinx.serialization.json.JsonElement {
    when (this) {
        is JsonNull -> return kotlinx.serialization.json.JsonNull
        is JsonObject -> {
            return kotlinx.serialization.json.JsonObject(this.entrySet()
                                                             .associate { it.key to it.value.intoKotlinJson() })
        }

        is JsonArray -> {
            return kotlinx.serialization.json.JsonArray(this.map { it.intoKotlinJson() })
        }

        is JsonPrimitive -> {
            if (this.isString)
                return kotlinx.serialization.json.JsonPrimitive(this.asString)
            if (this.isBoolean)
                return kotlinx.serialization.json.JsonPrimitive(this.asBoolean)
            return kotlinx.serialization.json.JsonPrimitive(this.asNumber)
        }

        else -> error("Unknown json variant $this")
    }
}

fun kotlinx.serialization.json.JsonElement.intoGson(): JsonElement {
    when (this) {
        is kotlinx.serialization.json.JsonNull -> return JsonNull.INSTANCE
        is kotlinx.serialization.json.JsonPrimitive -> {
            if (this.isString)
                return JsonPrimitive(this.content)
            if (this.content == "true")
                return JsonPrimitive(true)
            if (this.content == "false")
                return JsonPrimitive(false)
            return JsonPrimitive(LazilyParsedNumber(this.content))
        }

        is kotlinx.serialization.json.JsonObject -> {
            val obj = JsonObject()
            for ((k, v) in this) {
                obj.add(k, v.intoGson())
            }
            return obj
        }

        is kotlinx.serialization.json.JsonArray -> {
            val arr = JsonArray()
            for (v in this) {
                arr.add(v.intoGson())
            }
            return arr
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.function.Predicate
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import moe.nea.firmament.util.removeColorCodes

interface StringMatcher {
    fun matches(string: String): Boolean
    fun matches(text: Text): Boolean {
        return matches(text.string)
    }

    fun matches(nbt: NbtString): Boolean {
        val string = nbt.asString()
        val jsonStart = string.indexOf('{')
        val stringStart = string.indexOf('"')
        val isString = stringStart >= 0 && string.subSequence(0, stringStart).isBlank()
        val isJson = jsonStart >= 0 && string.subSequence(0, jsonStart).isBlank()
        if (isString || isJson)
            return matches(Text.Serialization.fromJson(string) ?: return false)
        return matches(string)
    }

    class Equals(input: String, val stripColorCodes: Boolean) : StringMatcher {
        private val expected = if (stripColorCodes) input.removeColorCodes() else input
        override fun matches(string: String): Boolean {
            return expected == (if (stripColorCodes) string.removeColorCodes() else string)
        }
    }

    class Pattern(patternWithColorCodes: String, val stripColorCodes: Boolean) : StringMatcher {
        private val regex: Predicate<String> = patternWithColorCodes.toPattern().asMatchPredicate()
        override fun matches(string: String): Boolean {
            return regex.test(if (stripColorCodes) string.removeColorCodes() else string)
        }
    }

    companion object {
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

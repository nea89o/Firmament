/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import java.util.*
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString

class LegacyTagParser private constructor(string: String) {
    data class TagParsingException(val baseString: String, val offset: Int, val mes0: String) :
        Exception("$mes0 at $offset in `$baseString`.")

    class StringRacer(val backing: String) {
        var idx = 0
        val stack = Stack<Int>()

        fun pushState() {
            stack.push(idx)
        }

        fun popState() {
            idx = stack.pop()
        }

        fun discardState() {
            stack.pop()
        }

        fun peek(count: Int): String {
            return backing.substring(minOf(idx, backing.length), minOf(idx + count, backing.length))
        }

        fun finished(): Boolean {
            return peek(1).isEmpty()
        }

        fun peekReq(count: Int): String? {
            val p = peek(count)
            if (p.length != count)
                return null
            return p
        }

        fun consumeCountReq(count: Int): String? {
            val p = peekReq(count)
            if (p != null)
                idx += count
            return p
        }

        fun tryConsume(string: String): Boolean {
            val p = peek(string.length)
            if (p != string)
                return false
            idx += p.length
            return true
        }

        fun consumeWhile(shouldConsumeThisString: (String) -> Boolean): String {
            var lastString: String = ""
            while (true) {
                val nextString = lastString + peek(1)
                if (!shouldConsumeThisString(nextString)) {
                    return lastString
                }
                idx++
                lastString = nextString
            }
        }

        fun expect(search: String, errorMessage: String) {
            if (!tryConsume(search))
                error(errorMessage)
        }

        fun error(errorMessage: String): Nothing {
            throw TagParsingException(backing, idx, errorMessage)
        }

    }

    val racer = StringRacer(string)
    val baseTag = parseTag()

    companion object {
        val digitRange = "0123456789-"
        fun parse(string: String): NbtCompound {
            return LegacyTagParser(string).baseTag
        }
    }

    fun skipWhitespace() {
        racer.consumeWhile { Character.isWhitespace(it.last()) } // Only check last since other chars are always checked before.
    }

    fun parseTag(): NbtCompound {
        skipWhitespace()
        racer.expect("{", "Expected '{’ at start of tag")
        skipWhitespace()
        val tag = NbtCompound()
        while (!racer.tryConsume("}")) {
            skipWhitespace()
            val lhs = parseIdentifier()
            skipWhitespace()
            racer.expect(":", "Expected ':' after identifier in tag")
            skipWhitespace()
            val rhs = parseAny()
            tag.put(lhs, rhs)
            racer.tryConsume(",")
            skipWhitespace()
        }
        return tag
    }

    private fun parseAny(): NbtElement {
        skipWhitespace()
        val nextChar = racer.peekReq(1) ?: racer.error("Expected new object, found EOF")
        return when {
            nextChar == "{" -> parseTag()
            nextChar == "[" -> parseList()
            nextChar == "\"" -> parseStringTag()
            nextChar.first() in (digitRange) -> parseNumericTag()
            else -> racer.error("Unexpected token found. Expected start of new element")
        }
    }

    fun parseList(): NbtList {
        skipWhitespace()
        racer.expect("[", "Expected '[' at start of tag")
        skipWhitespace()
        val list = NbtList()
        while (!racer.tryConsume("]")) {
            skipWhitespace()
            racer.pushState()
            val lhs = racer.consumeWhile { it.all { it in digitRange } }
            skipWhitespace()
            if (!racer.tryConsume(":") || lhs.isEmpty()) { // No prefixed 0:
                racer.popState()
                list.add(parseAny()) // Reparse our number (or not a number) as actual tag
            } else {
                racer.discardState()
                skipWhitespace()
                list.add(parseAny()) // Ignore prefix indexes. They should not be generated out of order by any vanilla implementation (which is what NEU should export). Instead append where it appears in order.
            }
            skipWhitespace()
            racer.tryConsume(",")
        }
        return list
    }

    fun parseQuotedString(): String {
        skipWhitespace()
        racer.expect("\"", "Expected '\"' at string start")
        val sb = StringBuilder()
        while (true) {
            when (val peek = racer.consumeCountReq(1)) {
                "\"" -> break
                "\\" -> {
                    val escaped = racer.consumeCountReq(1) ?: racer.error("Unfinished backslash escape")
                    if (escaped != "\"" && escaped != "\\") {
                        // Surprisingly i couldn't find unicode escapes to be generated by the original minecraft 1.8.9 implementation
                        racer.idx--
                        racer.error("Invalid backslash escape '$escaped'")
                    }
                    sb.append(escaped)
                }

                null -> racer.error("Unfinished string")
                else -> {
                    sb.append(peek)
                }
            }
        }
        return sb.toString()
    }

    fun parseStringTag(): NbtString {
        return NbtString.of(parseQuotedString())
    }

    object Patterns {
        val DOUBLE = "([-+]?[0-9]*\\.?[0-9]+)[d|D]".toRegex()
        val FLOAT = "([-+]?[0-9]*\\.?[0-9]+)[f|F]".toRegex()
        val BYTE = "([-+]?[0-9]+)[b|B]".toRegex()
        val LONG = "([-+]?[0-9]+)[l|L]".toRegex()
        val SHORT = "([-+]?[0-9]+)[s|S]".toRegex()
        val INTEGER = "([-+]?[0-9]+)".toRegex()
        val DOUBLE_UNTYPED = "([-+]?[0-9]*\\.?[0-9]+)".toRegex()
        val ROUGH_PATTERN = "[-+]?[0-9]*\\.?[0-9]*[dDbBfFlLsS]?".toRegex()
    }

    fun parseNumericTag(): AbstractNbtNumber {
        skipWhitespace()
        val textForm = racer.consumeWhile { Patterns.ROUGH_PATTERN.matchEntire(it) != null }
        if (textForm.isEmpty()) {
            racer.error("Expected numeric tag (starting with either -, +, . or a digit")
        }
        val floatMatch = Patterns.FLOAT.matchEntire(textForm)
        if (floatMatch != null) {
            return NbtFloat.of(floatMatch.groups[1]!!.value.toFloat())
        }
        val byteMatch = Patterns.BYTE.matchEntire(textForm)
        if (byteMatch != null) {
            return NbtByte.of(byteMatch.groups[1]!!.value.toByte())
        }
        val longMatch = Patterns.LONG.matchEntire(textForm)
        if (longMatch != null) {
            return NbtLong.of(longMatch.groups[1]!!.value.toLong())
        }
        val shortMatch = Patterns.SHORT.matchEntire(textForm)
        if (shortMatch != null) {
            return NbtShort.of(shortMatch.groups[1]!!.value.toShort())
        }
        val integerMatch = Patterns.INTEGER.matchEntire(textForm)
        if (integerMatch != null) {
            return NbtInt.of(integerMatch.groups[1]!!.value.toInt())
        }
        val doubleMatch = Patterns.DOUBLE.matchEntire(textForm) ?: Patterns.DOUBLE_UNTYPED.matchEntire(textForm)
        if (doubleMatch != null) {
            return NbtDouble.of(doubleMatch.groups[1]!!.value.toDouble())
        }
        throw IllegalStateException("Could not properly parse numeric tag '$textForm', despite passing rough verification. This is a bug in the LegacyTagParser")
    }

    private fun parseIdentifier(): String {
        skipWhitespace()
        if (racer.peek(1) == "\"") {
            return parseQuotedString()
        }
        return racer.consumeWhile {
            val x = it.last()
            x != ':' && !Character.isWhitespace(x)
        }
    }

}

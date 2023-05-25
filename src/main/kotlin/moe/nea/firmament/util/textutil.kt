/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.util

import net.minecraft.text.LiteralTextContent
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import moe.nea.firmament.Firmament


class TextMatcher(text: Text) {
    data class State(
        var iterator: MutableList<Text>,
        var currentText: Text?,
        var offset: Int,
        var textContent: String,
    )

    var state = State(
        mutableListOf(text),
        null,
        0,
        ""
    )

    fun pollChunk(): Boolean {
        val firstOrNull = state.iterator.removeFirstOrNull() ?: return false
        state.offset = 0
        state.currentText = firstOrNull
        state.textContent = when (val content = firstOrNull.content) {
            is LiteralTextContent -> content.string
            TextContent.EMPTY -> ""
            else -> {
                Firmament.logger.warn("TextContent of type ${content.javaClass} not understood.")
                return false
            }
        }
        state.iterator.addAll(0, firstOrNull.siblings)
        return true
    }

    fun pollChunks(): Boolean {
        while (state.offset !in state.textContent.indices) {
            if (!pollChunk()) {
                return false
            }
        }
        return true
    }

    fun pollChar(): Char? {
        if (!pollChunks()) return null
        return state.textContent[state.offset++]
    }


    fun expectString(string: String): Boolean {
        var found = ""
        while (found.length < string.length) {
            if (!pollChunks()) return false
            val takeable = state.textContent.drop(state.offset).take(string.length - found.length)
            state.offset += takeable.length
            found += takeable
        }
        return found == string
    }
}


val Text.unformattedString
    get() = string.replace("§.".toRegex(), "")


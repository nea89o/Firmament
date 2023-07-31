/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import net.minecraft.text.LiteralTextContent
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import net.minecraft.text.TranslatableTextContent
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


fun Text.transformEachRecursively(function: (Text) -> Text): Text {
    val c = this.content
    if (c is TranslatableTextContent) {
        return Text.translatableWithFallback(c.key, c.fallback, *c.args.map {
            (if (it is Text) it else Text.literal(it.toString())).transformEachRecursively(function)
        }.toTypedArray()).also { new ->
            new.style = this.style
            new.siblings.clear()
            this.siblings.forEach { child ->
                new.siblings.add(child.transformEachRecursively(function))
            }
        }
    }
    return function(this.copy().also { it.siblings.clear() }).also { tt ->
        this.siblings.forEach {
            tt.siblings.add(it.transformEachRecursively(function))
        }
    }
}

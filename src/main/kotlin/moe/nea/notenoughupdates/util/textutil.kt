package moe.nea.notenoughupdates.util

import net.minecraft.text.LiteralTextContent
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import moe.nea.notenoughupdates.NotEnoughUpdates


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
                NotEnoughUpdates.logger.warn("TextContent of type ${content.javaClass} not understood.")
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


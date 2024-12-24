package moe.nea.firmament.util

import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Formatting
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
			is PlainTextContent.Literal -> content.string
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

val formattingChars = "kmolnrKMOLNR".toSet()
fun CharSequence.removeColorCodes(keepNonColorCodes: Boolean = false): String {
	var nextParagraph = indexOf('§')
	if (nextParagraph < 0) return this.toString()
	val stringBuffer = StringBuilder(this.length)
	var readIndex = 0
	while (nextParagraph >= 0) {
		stringBuffer.append(this, readIndex, nextParagraph)
		if (keepNonColorCodes && nextParagraph + 1 < length && this[nextParagraph + 1] in formattingChars) {
			readIndex = nextParagraph
			nextParagraph = indexOf('§', startIndex = readIndex + 1)
		} else {
			readIndex = nextParagraph + 2
			nextParagraph = indexOf('§', startIndex = readIndex)
		}
		if (readIndex > this.length)
			readIndex = this.length
	}
	stringBuffer.append(this, readIndex, this.length)
	return stringBuffer.toString()
}

val Text.unformattedString: String
	get() = string.removeColorCodes() // TODO: maybe shortcircuit this with .visit

val Text.directLiteralStringContent: String? get() = (this.content as? PlainTextContent)?.string()

fun Text.getLegacyFormatString() =
	run {
		val sb = StringBuilder()
		for (component in iterator()) {
			sb.append(component.style.color?.toChatFormatting()?.toString() ?: "§r")
			sb.append(component.directLiteralStringContent)
			sb.append("§r")
		}
		sb.toString()
	}

private val textColorLUT = Formatting.entries
	.mapNotNull { formatting -> formatting.colorValue?.let { it to formatting } }
	.toMap()

fun TextColor.toChatFormatting(): Formatting? {
	return textColorLUT[this.rgb]
}

fun Text.iterator(): Sequence<Text> {
	return sequenceOf(this) + siblings.asSequence()
		.flatMap { it.iterator() } // TODO: in theory we want to properly inherit styles here
}

fun Text.allSiblings(): List<Text> = listOf(this) + siblings.flatMap { it.allSiblings() }

fun MutableText.withColor(formatting: Formatting): MutableText = this.styled {
	it.withColor(formatting)
		.withItalic(false)
		.withBold(false)
}

fun MutableText.blue() = withColor(Formatting.BLUE)
fun MutableText.aqua() = withColor(Formatting.AQUA)
fun MutableText.lime() = withColor(Formatting.GREEN)
fun MutableText.darkGreen() = withColor(Formatting.DARK_GREEN)
fun MutableText.purple() = withColor(Formatting.DARK_PURPLE)
fun MutableText.pink() = withColor(Formatting.LIGHT_PURPLE)
fun MutableText.yellow() = withColor(Formatting.YELLOW)
fun MutableText.grey() = withColor(Formatting.GRAY)
fun MutableText.red() = withColor(Formatting.RED)
fun MutableText.white() = withColor(Formatting.WHITE)
fun MutableText.bold(): MutableText = styled { it.withBold(true) }


fun MutableText.clickCommand(command: String): MutableText {
	require(command.startsWith("/"))
	return this.styled {
		it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
	}
}

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

fun tr(key: String, default: String): MutableText = error("Compiler plugin did not run.")
fun trResolved(key: String, vararg args: Any): MutableText = Text.stringifiedTranslatable(key, *args)


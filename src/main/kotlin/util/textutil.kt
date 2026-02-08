package moe.nea.firmament.util

import java.util.Optional
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.ChatFormatting


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

fun FormattedCharSequence.reconstitute(): MutableComponent {
	val base = Component.literal("")
	base.setStyle(Style.EMPTY.withItalic(false))
	var lastColorCode = Style.EMPTY
	val text = StringBuilder()
	this.accept { index, style, codePoint ->
		if (style != lastColorCode) {
			if (text.isNotEmpty())
				base.append(Component.literal(text.toString()).setStyle(lastColorCode))
			lastColorCode = style
			text.clear()
		}
		text.append(codePoint.toChar())
		true
	}
	if (text.isNotEmpty())
		base.append(Component.literal(text.toString()).setStyle(lastColorCode))
	return base

}

fun FormattedText.reconstitute(): MutableComponent {
	val base = Component.literal("")
	base.setStyle(Style.EMPTY.withItalic(false))
	var lastColorCode = Style.EMPTY
	val text = StringBuilder()
	this.visit({ style, string ->
		if (style != lastColorCode) {
			if (text.isNotEmpty())
				base.append(Component.literal(text.toString()).setStyle(lastColorCode))
			lastColorCode = style
			text.clear()
		}
		text.append(string)
		Optional.empty<Unit>()
	}, Style.EMPTY)
	if (text.isNotEmpty())
		base.append(Component.literal(text.toString()).setStyle(lastColorCode))
	return base

}

val Component.unformattedString: String
	get() = string.removeColorCodes() // TODO: maybe shortcircuit this with .visit

val Component.directLiteralStringContent: String? get() = (this.contents as? PlainTextContents)?.text()

fun Component.getLegacyFormatString(trimmed: Boolean = false): String =
	run {
		var lastCode = "§r"
		val sb = StringBuilder()
		fun appendCode(code: String) {
			if (code != lastCode || !trimmed) {
				sb.append(code)
				lastCode = code
			}
		}
		for (component in iterator()) {
			if (component.directLiteralStringContent.isNullOrEmpty() && component.siblings.isEmpty()) {
				continue
			}
			appendCode(component.style.let { style ->
				var color = style.color?.toChatFormatting()?.toString() ?: "§r"
				if (style.isBold)
					color += LegacyFormattingCode.BOLD.formattingCode
				if (style.isItalic)
					color += LegacyFormattingCode.ITALIC.formattingCode
				if (style.isUnderlined)
					color += LegacyFormattingCode.UNDERLINE.formattingCode
				if (style.isObfuscated)
					color += LegacyFormattingCode.OBFUSCATED.formattingCode
				if (style.isStrikethrough)
					color += LegacyFormattingCode.STRIKETHROUGH.formattingCode
				color
			})
			sb.append(component.directLiteralStringContent)
			if (!trimmed)
				appendCode("§r")
		}
		sb.toString()
	}.also {
		var it = it
		if (trimmed) {
			it = it.removeSuffix("§r")
			if (it.length == 2 && it.startsWith("§"))
				it = ""
		}
		it
	}

private val textColorLUT = ChatFormatting.entries
	.mapNotNull { formatting -> formatting.color?.let { it to formatting } }
	.toMap()

fun TextColor.toChatFormatting(): ChatFormatting? {
	return textColorLUT[this.value]
}

fun Component.iterator(): Sequence<Component> {
	return sequenceOf(this) + siblings.asSequence()
		.flatMap { it.iterator() } // TODO: in theory we want to properly inherit styles here
}

fun Component.allSiblings(): List<Component> = listOf(this) + siblings.flatMap { it.allSiblings() }

fun MutableComponent.withColor(formatting: ChatFormatting): MutableComponent = this.withStyle {
	it.withColor(formatting)
		.withItalic(false)
		.withBold(false)
}

fun MutableComponent.blue() = withColor(ChatFormatting.BLUE)
fun MutableComponent.aqua() = withColor(ChatFormatting.AQUA)
fun MutableComponent.lime() = withColor(ChatFormatting.GREEN)
fun MutableComponent.darkGreen() = withColor(ChatFormatting.DARK_GREEN)
fun MutableComponent.purple() = withColor(ChatFormatting.DARK_PURPLE)
fun MutableComponent.pink() = withColor(ChatFormatting.LIGHT_PURPLE)
fun MutableComponent.yellow() = withColor(ChatFormatting.YELLOW)
fun MutableComponent.gold() = withColor(ChatFormatting.GOLD)
fun MutableComponent.grey() = withColor(ChatFormatting.GRAY)
fun MutableComponent.darkGrey() = withColor(ChatFormatting.DARK_GRAY)
fun MutableComponent.red() = withColor(ChatFormatting.RED)
fun MutableComponent.white() = withColor(ChatFormatting.WHITE)
fun MutableComponent.bold(): MutableComponent = withStyle { it.withBold(true) }
fun MutableComponent.hover(text: Component): MutableComponent = withStyle { it.withHoverEvent(HoverEvent.ShowText(text)) }
fun MutableComponent.boolColour(
    bool: Boolean,
    ifTrue: ChatFormatting = ChatFormatting.GREEN,
    ifFalse: ChatFormatting = ChatFormatting.DARK_RED
) =
	if (bool) withColor(ifTrue) else withColor(ifFalse)

fun MutableComponent.clickCommand(command: String): MutableComponent {
	require(command.startsWith("/"))
	return this.withStyle {
		it.withClickEvent(ClickEvent.RunCommand(command))
	}
}

fun MutableComponent.prepend(text: Component): MutableComponent {
	siblings.addFirst(text)
	return this
}

fun Component.transformEachRecursively(function: (Component) -> Component): Component {
	val c = this.contents
	if (c is TranslatableContents) {
		return Component.translatableWithFallback(c.key, c.fallback, *c.args.map {
			(it as? Component ?: Component.literal(it.toString())).transformEachRecursively(function)
		}.toTypedArray()).also { new ->
			new.style = this.style
			new.siblings.clear()
			val new = function(new)
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

fun tr(key: String, default: String): MutableComponent = error("Compiler plugin did not run.")
fun trResolved(key: String, vararg args: Any): MutableComponent = Component.translatableEscape(key, *args) // TODO: handle null args
fun titleCase(str: String): String {
	return str
		.lowercase()
		.replace("_", " ")
		.split(" ")
		.joinToString(" ") { word ->
			word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
		}
}



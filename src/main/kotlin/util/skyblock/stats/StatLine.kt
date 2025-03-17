package moe.nea.firmament.util.skyblock.stats

import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.withColor
import net.minecraft.text.Style
import net.minecraft.text.Text
import util.skyblock.stats.StatFormatting
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.useMatch

data class StatLine(
	val stat: StatFormatting,
	val value: Double,
	val modifiers: List<Text> = listOf(),
	val loreIndex: Int = -1,
) {
	val statName get() = stat.name

	fun formatValue() =
		Text.literal(
			FirmFormatters.formatCommas(value, 1, includeSign = true) + stat.postFix + " "
		).setStyle(Style.EMPTY.withColor(stat.color))

	fun reconstitute(abbreviateTo: Int = Int.MAX_VALUE): Text =
		Text.literal("").setStyle(Style.EMPTY.withItalic(false))
			.append(Text.literal("${abbreviate(abbreviateTo)}: ").grey())
			.append(formatValue())
			.also { modifiers.forEach(it::append) }

	/*
	TODO: fix up the formatting of these
	Expected: StatBlock(indexedByName={Speed=StatLine(stat=StatFormatting(name=Speed, postFix=, color=§a, isStarAffected=true), value=5.0, modifiers=[literal{(+5)}[style={color=blue}]], loreIndex=0), Foraging Wisdom=StatLine(stat=StatFormatting(name=Foraging Wisdom, postFix=, color=§a, isStarAffected=true), value=1.5, modifiers=[literal{(+1.5)}[style={color=blue}]], loreIndex=1)}, startIndex=0, endIndex=2, modifiedLines={})
	Actual  : StatBlock(indexedByName={Speed=StatLine(stat=StatFormatting(name=Speed, postFix=, color=§a, isStarAffected=true), value=5.0, modifiers=[literal{(+5) }[style={color=blue,!bold,!italic}]], loreIndex=0), Foraging Wisdom=StatLine(stat=StatFormatting(name=Foraging Wisdom, postFix=, color=§a, isStarAffected=true), value=1.2, modifiers=[literal{(+1.2) }[style={color=blue,!bold,!italic}]], loreIndex=1)}, startIndex=0, endIndex=2, modifiedLines={})*/
	fun addStat(amount: Double, buffKind: BuffKind): StatLine {
		val formattedAmount = FirmFormatters.formatCommas(amount, 1, includeSign = true)
		val modifierText = Text.literal(
			buffKind.prefix + formattedAmount + stat.postFix + buffKind.postFix + " ")
			.withColor(buffKind.color)
		return copy(
			value = value + amount,
			modifiers = modifiers +
				if (buffKind.isHidden) emptyList()
				else listOf(modifierText))
	}

	private fun abbreviate(abbreviateTo: Int): String {
		if (abbreviateTo >= statName.length) return statName
		val segments = statName.split(" ")
		return segments.joinToString(" ") {
			it.substring(0, maxOf(1, abbreviateTo / segments.size))
		}
	}

	companion object {
		private val statLabelRegex = "(?<statName>.*): ".toPattern()
		fun fromLoreLine(line: Text): StatLine? {
			val sibs = line.siblings
			if (sibs.size < 2) return null
			val stat = sibs.first() ?: return null
			if (stat.style.color != TextColor.fromFormatting(Formatting.GRAY)) return null
			val statLabel = stat.directLiteralStringContent ?: return null
			val statName = statLabelRegex.useMatch(statLabel) { group("statName") } ?: return null
			return StatLine(StatFormatting.findForName(statName),
			                sibs[1]?.directLiteralStringContent?.trim(' ', 's', '%', '+')?.toDoubleOrNull() ?: 0.0,
			                sibs.subList(2, sibs.size))
		}
	}
}

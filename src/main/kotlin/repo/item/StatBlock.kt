package moe.nea.firmament.repo.item

import kotlin.collections.forEach
import kotlin.collections.plus
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import moe.nea.firmament.repo.SBItemStack.Companion.BuffKind
import moe.nea.firmament.repo.SBItemStack.Companion.StatFormatting
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

data class StatBlock(
	val indexedByName: Map<String, StatLine>,
	val startIndex: Int,
	val endIndex: Int,
	private val modifiedLines: MutableMap<String, StatLine> = mutableMapOf()
) {

	/**
	 * Note that the returned stat line must be created by copying from the original stat line (to keep indexes in sync).
	 */
	fun modify(statName: String, mod: (StatLine) -> StatLine) {
		val existing = modifiedLines[statName] ?: indexedByName[statName] ?: StatLine(findStatFormatting(statName), 0.0)
		modifiedLines[statName] = mod(existing)
	}

	fun applyModifications(lore: MutableList<Text>) {
		if (modifiedLines.isEmpty()) return
		var nextAppendIndex = endIndex
		if (startIndex < 0) // No existing stat block, insert the after space.
			lore.add(0, Text.literal(""))
		modifiedLines.values.forEach { line ->
			val loreLine = if (line.loreIndex < 0) {
				lore.add(nextAppendIndex, Text.literal(""))
				nextAppendIndex++
			} else line.loreIndex
			lore[loreLine] = line.reconstitute()
		}
	}

	companion object {
		fun fromLore(lore: List<Text>): StatBlock {
			val map = mutableMapOf<String, StatLine>()
			var start = -1
			var end = 0
			for ((index, text) in lore.withIndex()) {
				val statLine = parseStatLine(text)
					?.copy(loreIndex = index)
				if (statLine == null) {
					if (start < 0) continue
					else break
				}
				map[statLine.statName] = statLine
				if (start < 0)
					start = index
				end = index + 1
			}
			return StatBlock(map, start, end)
		}

		val allFormattingOverrides = listOf(
			StatFormatting("Sea Creature Chance", "%", Formatting.RED),
			StatFormatting("Strength", "", Formatting.RED),
			StatFormatting("Damage", "", Formatting.RED),
			StatFormatting("Bonus Attack Speed", "%", Formatting.RED),
			StatFormatting("Shot Cooldown", "s", Formatting.GREEN, false),
			StatFormatting("Ability Damage", "%", Formatting.RED),
			StatFormatting("Crit Damage", "%", Formatting.RED),
			StatFormatting("Crit Chance", "%", Formatting.RED),
			StatFormatting("Ability Damage", "%", Formatting.RED),
			StatFormatting("Trophy Fish Chance", "%", Formatting.GREEN),
			StatFormatting("Health", "", Formatting.GREEN),
			StatFormatting("Defense", "", Formatting.GREEN),
			StatFormatting("Fishing Speed", "", Formatting.GREEN),
			StatFormatting("Double Hook Chance", "%", Formatting.GREEN),
			StatFormatting("Mining Speed", "", Formatting.GREEN),
			StatFormatting("Mining Fortune", "", Formatting.GREEN),
			StatFormatting("Heat Resistance", "", Formatting.GREEN),
			StatFormatting("Swing Range", "", Formatting.GREEN),
			StatFormatting("Rift Time", "", Formatting.GREEN),
			StatFormatting("Speed", "", Formatting.GREEN),
			StatFormatting("Farming Fortune", "", Formatting.GREEN),
			StatFormatting("True Defense", "", Formatting.GREEN),
			StatFormatting("Mending", "", Formatting.GREEN),
			StatFormatting("Foraging Wisdom", "", Formatting.GREEN),
			StatFormatting("Farming Wisdom", "", Formatting.GREEN),
			StatFormatting("Foraging Fortune", "", Formatting.GREEN),
			StatFormatting("Magic Find", "", Formatting.GREEN),
			StatFormatting("Ferocity", "", Formatting.GREEN),
			StatFormatting("Bonus Pest Chance", "%", Formatting.GREEN),
			StatFormatting("Cold Resistance", "", Formatting.GREEN),
			StatFormatting("Pet Luck", "", Formatting.GREEN),
			StatFormatting("Fear", "", Formatting.GREEN),
			StatFormatting("Mana Regen", "%", Formatting.GREEN),
			StatFormatting("Rift Damage", "", Formatting.GREEN),
			StatFormatting("Hearts", "", Formatting.GREEN),
			StatFormatting("Vitality", "", Formatting.GREEN),
			// TODO: make this a repo json
		)
		val formattingOverrides = allFormattingOverrides.associateBy { it.name }
		fun findStatFormatting(name: String) =
			formattingOverrides[name] ?: StatFormatting(name, "", Formatting.GREEN)

		private val statLabelRegex = "(?<statName>.*): ".toPattern()
		private fun parseStatLine(line: Text): StatLine? {
			val sibs = line.siblings
			val stat = sibs.firstOrNull() ?: return null
			if (stat.style.color != TextColor.fromFormatting(Formatting.GRAY)) return null
			val statLabel = stat.directLiteralStringContent ?: return null
			val statName = statLabelRegex.useMatch(statLabel) { group("statName") } ?: return null
			return StatLine(findStatFormatting(statName),
			                sibs[1]?.directLiteralStringContent?.trim(' ', 's', '%', '+')?.toDoubleOrNull() ?: 0.0,
			                sibs.subList(2, sibs.size))
		}

	}

	data class StatLine(
		val stat: StatFormatting,
		val value: Double,
		val modifiers: List<Text> = listOf(),
		val loreIndex: Int = -1,
	) {
		fun formatValue() =
			Text.literal(FirmFormatters.formatCommas(
				value, 1, includeSign = true) + stat.postFix + " ")
				.setStyle(Style.EMPTY.withColor(stat.color))

		val statName get() = stat.name
		fun reconstitute(abbreviateTo: Int = Int.MAX_VALUE): Text =
			Text.literal("").setStyle(Style.EMPTY.withItalic(false))
				.append(Text.literal("${abbreviate(abbreviateTo)}: ").grey())
				.append(formatValue())
				.also { modifiers.forEach(it::append) }

		fun addStat(amount: Double, buffKind: BuffKind): StatLine {
			val formattedAmount = FirmFormatters.formatCommas(amount, 1, includeSign = true)
			return copy(
				value = value + amount,
				modifiers = modifiers +
					if (buffKind.isHidden) emptyList()
					else listOf(
						Text.literal(
							buffKind.prefix + formattedAmount +
								stat.postFix +
								buffKind.postFix + " ")
							.withColor(buffKind.color)))
		}

		private fun abbreviate(abbreviateTo: Int): String {
			if (abbreviateTo >= statName.length) return statName
			val segments = statName.split(" ")
			return segments.joinToString(" ") {
				it.substring(0, maxOf(1, abbreviateTo / segments.size))
			}
		}
	}
}

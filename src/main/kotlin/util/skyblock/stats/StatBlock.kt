package moe.nea.firmament.util.skyblock.stats

import util.skyblock.stats.StatFormatting
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.useMatch
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

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
		val existing =
			modifiedLines[statName] ?: indexedByName[statName]
			?: StatLine(StatFormatting.findForName(statName), 0.0)
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
				val statLine = StatLine.fromLoreLine(text)
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
	}
}

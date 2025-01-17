package moe.nea.firmament.util

import java.util.Optional
import net.minecraft.client.gui.hud.InGameHud
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.Team
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.TickEvent

object ScoreboardUtil {
	var scoreboardLines: List<Text> = listOf()
	var simplifiedScoreboardLines: List<String> = listOf()

	@Subscribe
	fun onTick(event: TickEvent) {
		scoreboardLines = getScoreboardLinesUncached()
		simplifiedScoreboardLines = scoreboardLines.map { it.unformattedString }
	}

	private fun getScoreboardLinesUncached(): List<Text> {
		val scoreboard = MC.player?.scoreboard ?: return listOf()
		val activeObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return listOf()
		return scoreboard.getScoreboardEntries(activeObjective)
			.filter { !it.hidden() }
			.sortedWith(InGameHud.SCOREBOARD_ENTRY_COMPARATOR)
			.take(15).map {
				val team = scoreboard.getScoreHolderTeam(it.owner)
				val text = it.name()
				Team.decorateName(team, text)
			}
	}
}

fun Text.formattedString(): String {
	val sb = StringBuilder()
	visit(StringVisitable.StyledVisitor<Unit> { style, string ->
		val c = Formatting.byName(style.color?.name)
		if (c != null) {
			sb.append("§${c.code}")
		}
		if (style.isUnderlined) {
			sb.append("§n")
		}
		if (style.isBold) {
			sb.append("§l")
		}
		sb.append(string)
		Optional.empty()
	}, Style.EMPTY)
	return sb.toString().replace("§[^a-f0-9]".toRegex(), "")
}

package moe.nea.firmament.util

import java.util.Optional
import net.minecraft.client.gui.Gui
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Hud
import net.minecraft.network.chat.TextColor
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.TickEvent

object ScoreboardUtil {
	var scoreboardLines: List<Component> = listOf()
	var simplifiedScoreboardLines: List<String> = listOf()

	@Subscribe
	fun onTick(event: TickEvent) {
		scoreboardLines = getScoreboardLinesUncached()
		simplifiedScoreboardLines = scoreboardLines.map { it.unformattedString }
	}

	private fun getScoreboardLinesUncached(): List<Component> {
		val scoreboard = MC.instance.level?.scoreboard ?: return listOf()
		val activeObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return listOf()
		return scoreboard.listPlayerScores(activeObjective)
			.filter { !it.isHidden() }
			.sortedWith(Hud.SCORE_DISPLAY_ORDER)
			.take(15).map {
				val team = scoreboard.getPlayersTeam(it.owner)
				val text = it.ownerName()
				PlayerTeam.formatNameForTeam(team, text)
			}
	}
}

fun Component.formattedString(): String {
	val sb = StringBuilder()
	visit(FormattedText.StyledContentConsumer<Unit> { style, string ->
		val c = style.color?.toChatFormatting()
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

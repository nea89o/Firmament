package moe.nea.firmament.util

import java.util.Optional
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.Team
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun getScoreboardLines(): List<Text> {
    val scoreboard = MC.player?.scoreboard ?: return listOf()
    val activeObjective = scoreboard.getObjectiveForSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID) ?: return listOf()
    return scoreboard.getAllPlayerScores(activeObjective).reversed().take(15).map {
        val team = scoreboard.getPlayerTeam(it.playerName)
        Team.decorateName(team, Text.literal(it.playerName))
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

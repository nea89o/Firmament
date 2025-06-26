package moe.nea.firmament.util.tab

// Skidded from NEU :broken_heart:
// Credit: https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/8f43c38d5b17fc48f4c4af483eebb8fccfead04c/src/main/java/io/github/moulberry/notenoughupdates/util/TabListUtils.java (modified)

import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import moe.nea.firmament.mixins.accessor.AccessorPlayerListHud
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.getLegacyFormatString
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.scoreboard.Team
import net.minecraft.world.GameMode

object TablistUtils {

	private val playerOrdering: Ordering<PlayerListEntry?> = Ordering.from(PlayerComparator())

	class PlayerComparator : Comparator<PlayerListEntry?> {
		override fun compare(o1: PlayerListEntry?, o2: PlayerListEntry?): Int {
			val team1: Team? = o1?.scoreboardTeam
			val team2: Team? = o2?.scoreboardTeam
			return ComparisonChain.start().compareTrueFirst(
				o1?.gameMode !== GameMode.SPECTATOR, o2?.gameMode !== GameMode.SPECTATOR
			).compare(team1?.name ?: "", team2?.name ?: "").compare(o1?.profile?.name ?: "", o2?.profile?.name ?: "")
				.result()
		}
	}

	fun getTabList(): MutableList<String> {
	val network = MC.networkHandler ?: return ArrayList<String>()
	val players = playerOrdering.sortedCopy<PlayerListEntry>(network.playerList)
	val result: MutableList<String> = ArrayList<String>()

	for (info in players) {
		val name: String? = MC.inGameHud.playerListHud.getPlayerName(info).getLegacyFormatString()
		result.add(name!!)
	}

		val lines = ArrayList<String>()
		val footer =
			(MC.inGameHud.playerListHud as AccessorPlayerListHud).footer_firmament.getLegacyFormatString().split('\n')
		var seenBlank = false
		for (line in footer) {
			if (line == "§r§r§r§r§s§r" || line == "§r") {
				seenBlank = true //This is to emulate the space every other widget has for its lines
				continue
			}
			if (seenBlank) {
				lines.add(line)
				seenBlank = false
			} else {
				lines.add(" $line")
			}
		}
		return result
	}
}

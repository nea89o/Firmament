package moe.nea.firmament.util.skyblock

import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.ScoreboardUtil
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.TIME_PATTERN

object DungeonUtil {
	val isInDungeonIsland get() = SBData.skyblockLocation == SkyBlockIsland.DUNGEON
	private val timeElapsedRegex = "Time Elapsed: $TIME_PATTERN".toRegex()
	val isInActiveDungeon get() = isInDungeonIsland && ScoreboardUtil.simplifiedScoreboardLines.any { it.matches(
		timeElapsedRegex) }

/*Title:

§f§lSKYBLOCK§B§L CO-OP

' Late Spring 7th'
' §75:20am'
' §7⏣ §cThe Catacombs §7(M3)'
' §7♲ §7Ironman'
'       '
'Keys: §c■ §c✗ §8■ §a1x'
'Time Elapsed: §a46s'
'Cleared: §660% §8(105)'
'           '
'§e[B] §b151_Dragon §e2,062§c❤'
'§e[A] §6Lennart0312 §a17,165§c'
'§e[T] §b187i §a14,581§c❤'
'§e[H] §bFlameeke §a8,998§c❤'
'               '
'§ewww.hypixel.net'*/
}

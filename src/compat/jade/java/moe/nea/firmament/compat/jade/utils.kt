package moe.nea.firmament.compat.jade

import moe.nea.firmament.util.SBData

fun isOnMiningIsland(): Boolean =
	SBData.skyblockLocation?.hasCustomMining ?: false

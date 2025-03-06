package moe.nea.firmament.compat.jade

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.repo.MiningRepoData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.ErrorUtil
import net.minecraft.block.Block

object CurrentCustomBlockHolder {
	var customBlocks: Map<Block, MiningRepoData.CustomMiningBlock> = mapOf()

	fun refreshBlockInfo() {
		if (!isOnMiningIsland()) {
			customBlocks = mapOf()
			return
		}
		val blocks = RepoManager.miningData.customMiningBlocks
			.flatMap { customBlock ->
				// TODO: add a lifted helper method for this
				customBlock.blocks189.filter { it.isCurrentlyActive }
					.mapNotNull { it.block }
					.map { customBlock to it }
			}
			.groupBy { it.second }
		customBlocks = blocks.mapNotNull { (block, customBlocks) ->
			val singleMatch =
				ErrorUtil.notNullOr(customBlocks.singleOrNull()?.first,
				                    "Two custom blocks both want to supply custom mining behaviour for $block.") { return@mapNotNull null }
			block to singleMatch
		}.toMap()
	}

	@Subscribe
	fun onWorldSwap(event: SkyblockServerUpdateEvent) {
		refreshBlockInfo()
	}
}

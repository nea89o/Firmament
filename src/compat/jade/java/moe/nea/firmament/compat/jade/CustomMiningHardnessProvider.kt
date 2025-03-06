package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.block.Block
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.repo.MiningRepoData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.tr

object CustomMiningHardnessProvider : IBlockComponentProvider {

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


	override fun appendTooltip(
		tooltip: ITooltip,
		block: BlockAccessor,
		config: IPluginConfig?
	) {
		val customBlock = customBlocks[block.block]
			?: return
		if (customBlock.breakingPower > 0)
			tooltip.add(tr("firmament.jade.breaking_power", "Required Breaking Power: ${customBlock.breakingPower}"))
	}

	override fun getUid(): Identifier =
		Firmament.identifier("custom_mining_hardness")
}

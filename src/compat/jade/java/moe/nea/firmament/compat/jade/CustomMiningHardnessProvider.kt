package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.tr

object CustomMiningHardnessProvider : IBlockComponentProvider {

	override fun appendTooltip(
		tooltip: ITooltip,
		block: BlockAccessor,
		config: IPluginConfig?
	) {
		val customBlock = CustomFakeBlockProvider.getCustomBlock(block) ?: return
		if (customBlock.breakingPower > 0)
			tooltip.add(tr("firmament.jade.breaking_power", "Required Breaking Power: ${customBlock.breakingPower}"))
	}

	override fun getUid(): Identifier =
		Firmament.identifier("custom_mining_hardness")

	data class BreakingInfo(
		val blockPos: BlockPos, val stage: Int,
		val state: BlockState?,
	)

	var currentBreakingInfo: BreakingInfo? = null

	@JvmStatic
	fun setBreakingInfo(blockPos: BlockPos, stage: Int) {
		currentBreakingInfo = BreakingInfo(blockPos.toImmutable(), stage, MC.world?.getBlockState(blockPos))
	}

	@JvmStatic
	fun replaceBreakProgress(original: Float): Float {
		if (!isOnMiningIsland()) return original
		val pos = MC.interactionManager?.currentBreakingPos ?: return original
		val info = currentBreakingInfo
		if (info?.blockPos != pos || info.state != MC.world?.getBlockState(pos)) {
			currentBreakingInfo = null
			return 0F
		}
		// TODO: use linear extrapolation to guess how quickly this progresses between stages.
		// This would only be possible after one stage, but should make the remaining stages a bit smoother
		return info.stage / 10F
	}
}

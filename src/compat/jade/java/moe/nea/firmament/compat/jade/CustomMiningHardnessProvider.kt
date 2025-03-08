package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import kotlin.time.DurationUnit
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
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
		val ts: TimeMark = TimeMark.now()
	)

	var previousBreakingInfo: BreakingInfo? = null
	var currentBreakingInfo: BreakingInfo? = null

	@Subscribe
	fun clearInfoOnStopBreaking(event: TickEvent) {
		val isBreakingBlock = MC.interactionManager?.isBreakingBlock ?: false
		if (!isBreakingBlock) {
			previousBreakingInfo = null
			currentBreakingInfo = null
		}
	}

	@JvmStatic
	fun setBreakingInfo(blockPos: BlockPos, stage: Int) {
		previousBreakingInfo = currentBreakingInfo
		val state = MC.world?.getBlockState(blockPos)
		if (previousBreakingInfo?.let { it.state != state || it.blockPos != blockPos } ?: false)
			previousBreakingInfo == null
		currentBreakingInfo = BreakingInfo(blockPos.toImmutable(), stage, state)
		// For some reason hypixel initially sends a stage 10 packet, and then fixes it up with a stage 0 packet.
		// Ignore the stage 10 packet if we dont have any previous packets for this block.
		// This could in theory still have issues if someone perfectly stops breaking a block the tick it finishes and then does not break another block until it respawns, but i deem that to be too much of an edge case.
		if (stage == 10 && previousBreakingInfo == null) {
			previousBreakingInfo = null
			currentBreakingInfo = null
		}
	}

	@JvmStatic
	fun replaceBreakProgress(original: Float): Float {
		if (!isOnMiningIsland()) return original
		val pos = MC.interactionManager?.currentBreakingPos ?: return original
		val info = currentBreakingInfo
		if (info?.blockPos != pos || info.state != MC.world?.getBlockState(pos)) {
			currentBreakingInfo = null
			previousBreakingInfo = null
			return original
		}
		// TODO: improve this interpolation to work across all stages, to alleviate some of the jittery bar.
		// Maybe introduce a proper mining API that tracks the actual progress with some sort of FSM
		val interpolatedStage = previousBreakingInfo?.let { prev ->
			val timeBetweenTicks = (info.ts - prev.ts).toDouble(DurationUnit.SECONDS)
			val stagesPerUpdate = (info.stage - prev.stage).toDouble()
			if (stagesPerUpdate < 1) return@let null
			val stagesPerSecond = stagesPerUpdate / timeBetweenTicks
			info.stage + (info.ts.passedTime().toDouble(DurationUnit.SECONDS) * stagesPerSecond)
				.coerceAtMost(stagesPerUpdate)
		}?.toFloat()
		val stage = interpolatedStage ?: info.stage.toFloat()
		return stage / 10F
	}

	@JvmStatic
	fun replaceBlockBreakSpeed(original: Float): Float {
		if (isOnMiningIsland()) return 0F
		return original
	}
}

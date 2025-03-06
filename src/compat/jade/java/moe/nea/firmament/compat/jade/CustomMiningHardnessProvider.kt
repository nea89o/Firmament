package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
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
}

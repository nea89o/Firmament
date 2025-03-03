package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.util.SBData

class HardstoneProvider : IBlockComponentProvider {
	override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
		if (SBData.isOnSkyblock) {
			tooltip.add(drillIcon)
			// TODO: override jade breakability test to include breaking power of drills on mining islands
			tooltip.append(Text.of("Breaking Power 5"))
			tooltip.replace(Identifier.of("minecraft", "stone"), Text.literal("Hard Stone")) // this doesnt work
		}
	}

	override fun getUid(): Identifier {
		return "hardstone".jadeId()
	}
}

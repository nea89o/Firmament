package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.util.SBData

class MithrilProvider(val type: String) : IBlockComponentProvider {
	override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
		if (SBData.isOnSkyblock) { // why is there no utility to check if we are on an island with mithril am i dumb
			tooltip.add(drillIcon)
			tooltip.append(Text.of("Breaking Power 5"))
			tooltip.replace(Identifier.of("minecraft", type), Text.literal("Mithril $type")) // this doesnt work
		}
	}

	override fun getUid(): Identifier {
		return "mithril_$type".jadeId()
	}
}


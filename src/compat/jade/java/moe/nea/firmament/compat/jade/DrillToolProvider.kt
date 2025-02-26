package moe.nea.firmament.compat.jade

import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.util.Identifier

class DrillToolProvider : IBlockComponentProvider {
	override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
		TODO("Not yet implemented")
	}

	override fun getUid(): Identifier {
		TODO("Not yet implemented")
	}
}

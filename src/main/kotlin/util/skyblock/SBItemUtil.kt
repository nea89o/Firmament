package moe.nea.firmament.util.skyblock

import moe.nea.firmament.util.mc.DataComponentAccessor
import moe.nea.firmament.util.mc.RequiresComponents
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.renderingName
import moe.nea.firmament.util.unformattedString

object SBItemUtil {
	@RequiresComponents
	fun DataComponentAccessor.getSearchName(): String {
		val name = this.renderingName.unformattedString
		if (name.contains("Enchanted Book")) {
			val enchant = this.loreAccordingToNbt.firstOrNull()?.unformattedString
			if (enchant != null) return enchant
		}
		if (name.startsWith("[Lvl")) {
			val closing = name.indexOf(']')
			if (closing > 0)
				return name.substring(closing)
		}
		return name
	}
}

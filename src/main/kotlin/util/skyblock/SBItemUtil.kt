package moe.nea.firmament.util.skyblock

import net.minecraft.item.ItemStack
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

object SBItemUtil {
	fun ItemStack.getSearchName(): String {
		val name = this.name.unformattedString
		if (name.contains("Enchanted Book")) {
			val enchant = loreAccordingToNbt.firstOrNull()?.unformattedString
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

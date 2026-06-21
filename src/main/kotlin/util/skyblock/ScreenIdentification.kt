package moe.nea.firmament.util.skyblock

import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString


object ScreenIdentification {
	private var lastScreen: Screen? = null
	private var lastScreenType: ScreenType? = null

	fun getType(screen: Screen?): ScreenType? {
		if (screen == null) return null
		if (screen !== lastScreen) {
			lastScreenType = ScreenType.entries
				.find { it.detector(screen) }
			lastScreen = screen
		}
		return lastScreenType
	}
}

enum class ScreenType(val detector: (Screen) -> Boolean) {
	BAZAAR_ANY({
		it is ContainerScreen && (
			it.menu.getSlot(it.menu.rowCount * 9 - 4)
				.item
				.accessor()
				.displayNameAccordingToNbt
				.unformattedString == "Manage Orders"
				|| it.menu.getSlot(it.menu.rowCount * 9 - 5)
				.item
				.accessor()
				.loreAccordingToNbt
				.any {
					it.unformattedString == "To Bazaar"
				})
	}),
	ENCHANTMENT_GUIDE({
		it.title.unformattedString.endsWith("Enchantments Guide")
	}),
	SUPER_PAIRS({
		it.title.unformattedString.startsWith("Superpairs")
	}),
	EXPERIMENTATION_RNG_METER({
		it.title.unformattedString.contains("Experimentation Table RNG")
	}),
	DYE_COMPENDIUM({
		it.title.unformattedString.contains("Dye Compendium")
	})
}


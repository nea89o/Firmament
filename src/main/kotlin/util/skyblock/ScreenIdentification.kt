package moe.nea.firmament.util.skyblock

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

fun Screen.isBazaarUi(): Boolean {
	if (this !is GenericContainerScreen) return false
	val screenHandler = this.screenHandler ?: return false
	val manageOrderStack = screenHandler.inventory.getStack(screenHandler.rows * 9 - 4)
	if (manageOrderStack.displayNameAccordingToNbt.unformattedString == "Manage Orders") return true
	val toBazaarStack = screenHandler.inventory.getStack(screenHandler.rows * 9 - 5)
	return toBazaarStack.loreAccordingToNbt.any { it.unformattedString == "To Bazaar" }
}

fun Screen.isEnchantmentGuide(): Boolean {
	return title.unformattedString.endsWith("Enchantments Guide")
}

fun Screen.isSuperPairs(): Boolean {
	return title.unformattedString.startsWith("Superpairs")
}

fun Screen.isExperimentationRngMeter(): Boolean {
	return this.title.unformattedString.contains("Experimentation Table RNG")
}

fun Screen.isDyeCompendium(): Boolean {
	return this.title.unformattedString.contains("Dye Compendium")
}

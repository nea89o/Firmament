package moe.nea.firmament.util.skyblock

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

fun Screen.isBazaarUi(): Boolean {
	if (this !is GenericContainerScreen) return false
	val screenHandler = this.screenHandler ?: return false
	val desiredItemSlot = screenHandler.rows * 9 - 4
	val desiredItem = screenHandler.inventory.getStack(desiredItemSlot)
	return (desiredItem.displayNameAccordingToNbt.unformattedString == "Manage Orders" ||
		desiredItem.loreAccordingToNbt.any { it.unformattedString == "To Bazaar" })
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

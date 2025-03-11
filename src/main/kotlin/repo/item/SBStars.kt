package moe.nea.firmament.repo.item

import net.minecraft.item.ItemStack
import moe.nea.firmament.util.getUpgradeStars

object SBStars : SBItemProperty.State<Int>() {
	override fun applyToStack(
		stack: ItemStack,
		store: SBItemData,
		value: Int?
	): ItemStack {
		TODO()
	}

	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): Int? {
		return stack.getUpgradeStars()
	}
}

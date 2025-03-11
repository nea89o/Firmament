package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import net.minecraft.item.ItemStack

@AutoService(SBItemProperty::class)
object SBStackSize : SBItemProperty.State<Int>() {
	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): Int? {
		return stack.count
	}

	override fun applyToStack(
		stack: ItemStack,
		store: SBItemData,
		value: Int?
	): ItemStack {
		if (value != null)
			stack.count = value
		return stack
	}
}

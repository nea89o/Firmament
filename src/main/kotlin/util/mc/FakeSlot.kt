package moe.nea.firmament.util.mc

import util.mc.FakeInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class FakeSlot(
	stack: ItemStack,
	x: Int,
	y: Int
) : Slot(FakeInventory(stack), 0, x, y) {
	init {
		id = 0
	}
}

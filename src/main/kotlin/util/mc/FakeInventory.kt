package util.mc

import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

class FakeInventory(val stack: ItemStack) : Container {
	override fun clearContent() {
	}

	override fun getContainerSize(): Int {
		return 1
	}

	override fun isEmpty(): Boolean {
		return stack.isEmpty
	}

	override fun getItem(slot: Int): ItemStack {
		require(slot == 0)
		return stack
	}

	override fun removeItem(slot: Int, amount: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun removeItemNoUpdate(slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun setItem(slot: Int, stack: ItemStack) {
	}

	override fun setChanged() {
	}

	override fun stillValid(player: Player): Boolean {
		return true
	}
}

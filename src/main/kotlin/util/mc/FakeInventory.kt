package util.mc

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class FakeInventory(val stack: ItemStack) : Inventory {
	override fun clear() {
	}

	override fun size(): Int {
		return 1
	}

	override fun isEmpty(): Boolean {
		return stack.isEmpty
	}

	override fun getStack(slot: Int): ItemStack {
		require(slot == 0)
		return stack
	}

	override fun removeStack(slot: Int, amount: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun removeStack(slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun setStack(slot: Int, stack: ItemStack?) {
	}

	override fun markDirty() {
	}

	override fun canPlayerUse(player: PlayerEntity?): Boolean {
		return true
	}
}

package moe.nea.firmament.util.mc

import java.util.Spliterator
import java.util.Spliterators
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

val Inventory.indices get() = 0 until size()
val Inventory.iterableView
	get() = object : Iterable<ItemStack> {
		override fun spliterator(): Spliterator<ItemStack> {
			return Spliterators.spliterator(iterator(), size().toLong(), 0)
		}

		override fun iterator(): Iterator<ItemStack> {
			return object : Iterator<ItemStack> {
				var i = 0
				override fun hasNext(): Boolean {
					return i < size()
				}

				override fun next(): ItemStack {
					if (!hasNext()) throw NoSuchElementException()
					return getStack(i++)
				}
			}
		}
	}

package moe.nea.firmament.repo.item

import io.github.moulberry.repo.data.NEUItem
import net.minecraft.item.ItemStack
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.util.compatloader.CompatLoader

/**
 * A property of a skyblock item. Not every skyblock item must have this property, but some should.
 *
 * Access to this class should be limited to [State.bind] and [SBItemData.getData].
 * @see State
 */
abstract class SBItemProperty<T> {
	data class BoundState<T>(val property: State<T>, val data: T) {
		fun applyTo(store: SBItemData) {
			store.set(property, data)
		}
	}

	// TODO: Actually implement and make use of this method.
	/**
	 * Extract this property's state from a [NEUItem]. If this method returns something, it may be equivalent to [fromStack] with the neu item resolved to an item stack according to [asItemStack], but *should not* instantiate an item stack. This method may return null to indicate that it needs a fully constructed item stack to extract a property. This method return one value and then later return another value from [fromStack], but behaviour is generally discouraged.
	 */
	open fun fromNeuItem(neuItem: NEUItem, store: SBItemData): T? {
		return null
	}

	/**
	 * Extract this property's state from an [ItemStack]. This should be fully reversible (i.e. all info used to in [fromStack] needs to be set by [State.applyToStack].
	 */
	abstract fun fromStack(stack: ItemStack, store: SBItemData): T?

	/**
	 * A property of a skyblock item that carriers state. Unlike a plain [SBItemProperty] these modifiers can be used
	 * to change the state of an item, including its rendering as a vanilla [ItemStack].
	 */
	abstract class State<T> : SBItemProperty<T>() {
		/**
		 * Apply the stored info back to the item stack. If possible [stack] should be modified and returned directly,
		 * instead of creating a new [ItemStack] instance. Information stored here should be recovered using [fromStack].
		 */
		abstract fun applyToStack(stack: ItemStack, store: SBItemData, value: T?): ItemStack
		fun bind(data: T) = BoundState(this, data)
	}

	/**
	 * The order of this property relative to other properties. Lower values get computed first, so higher values may
	 * rely on their data being stored already (if that item stack has any of that data), and they can overwrite the
	 * rendering of the lower states.
	 */
	open val order: Int get() = 0

	companion object {
		val loader = CompatLoader<SBItemProperty<*>>(SBItemProperty::class)
		val allProperties by lazy {
			loader.allValidInstances.sortedBy { it.order }
		}
		val allStates by lazy {
			allProperties.filterIsInstance<State<*>>()
		}
	}
}

package moe.nea.firmament.events

import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.util.collections.WeakCache

// TODO: assert an order on these events
data class CustomItemModelEvent(
	val itemStack: ItemStack,
	var overrideModel: Identifier? = null,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<CustomItemModelEvent>() {
		val cache = WeakCache.memoize("ItemModelIdentifier", ::getModelIdentifier0)

		@JvmStatic
		fun getModelIdentifier(itemStack: ItemStack?): Identifier? {
			if (itemStack == null) return null
			return cache.invoke(itemStack).getOrNull()
		}

		fun getModelIdentifier0(itemStack: ItemStack): Optional<Identifier> {
			// TODO: add an error / warning if the model does not exist
			return Optional.ofNullable(publish(CustomItemModelEvent(itemStack)).overrideModel)
		}
	}

	fun overrideIfExists(overrideModel: Identifier) {
		this.overrideModel = overrideModel
	}
}

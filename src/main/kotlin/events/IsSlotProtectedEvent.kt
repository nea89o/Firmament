package moe.nea.firmament.events

import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.ClickType
import moe.nea.firmament.features.inventory.SlotLocking
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.hover
import moe.nea.firmament.util.red
import moe.nea.firmament.util.tr

data class IsSlotProtectedEvent(
    val slot: Slot?,
    val actionType: ClickType,
    var isProtected: Boolean,
    val itemStackOverride: ItemStack?,
    val origin: MoveOrigin,
    var silent: Boolean = false,
) : FirmamentEvent() {
	val itemStack get() = itemStackOverride ?: slot!!.item

	fun protect() {
		if (!isProtected) {
			silent = false
		}
		isProtected = true
	}

	fun protectSilent() {
		if (!isProtected) {
			silent = true
		}
		isProtected = true
	}

	enum class MoveOrigin {
		DROP_FROM_HOTBAR,
		SALVAGE,
		INVENTORY_MOVE
		;
	}

	companion object : FirmamentEventBus<IsSlotProtectedEvent>() {
		@JvmStatic
		@JvmOverloads
		fun shouldBlockInteraction(
            slot: Slot?, action: ClickType,
            origin: MoveOrigin,
            itemStackOverride: ItemStack? = null,
		): Boolean {
			if (slot == null && itemStackOverride == null) return false
			val event = IsSlotProtectedEvent(slot, action, false, itemStackOverride, origin)
			publish(event)
			if (event.isProtected && !event.silent) {
				if (SlotLocking.TConfig.dropMessage) {
					MC.sendChat(
						tr("firmament.protectitem", "Firmament protected your item: ${event.itemStack.hoverName}.\n")
							.red()
							.append(tr("firmament.protectitem.hoverhint", "Hover for more info.").grey())
							.hover(
								tr(
									"firmament.protectitem.hint",
									"To unlock this item use the Lock Slot or Lock Item keybind from Firmament while hovering over this item. If this is a bound slot, you can use disable the Lock Bound Slots setting."
								)
							)
					)
				}
				if (SlotLocking.TConfig.dropSound) {
					CommonSoundEffects.playFailure()
				}
			}
			return event.isProtected
		}
	}
}

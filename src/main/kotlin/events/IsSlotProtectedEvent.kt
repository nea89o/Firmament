package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.hover
import moe.nea.firmament.util.red
import moe.nea.firmament.util.tr

data class IsSlotProtectedEvent(
	val slot: Slot?,
	val actionType: SlotActionType,
	var isProtected: Boolean,
	val itemStackOverride: ItemStack?,
	val origin: MoveOrigin,
	var silent: Boolean = false,
) : FirmamentEvent() {
	val itemStack get() = itemStackOverride ?: slot!!.stack

	fun protect() {
		isProtected = true
		silent = false
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
			slot: Slot?, action: SlotActionType,
			origin: MoveOrigin,
			itemStackOverride: ItemStack? = null,
		): Boolean {
			if (slot == null && itemStackOverride == null) return false
			val event = IsSlotProtectedEvent(slot, action, false, itemStackOverride, origin)
			publish(event)
			if (event.isProtected && !event.silent) {
				MC.sendChat(tr("firmament.protectitem", "Firmament protected your item: ${event.itemStack.name}.\n")
					            .red()
					            .append(tr("firmament.protectitem.hoverhint", "Hover for more info.").grey())
					            .hover(tr("firmament.protectitem.hint",
					                      "To unlock this item use the Lock Slot or Lock Item keybind from Firmament while hovering over this item.")))
				CommonSoundEffects.playFailure()
			}
			return event.isProtected
		}
	}
}

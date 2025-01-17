package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC

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
				MC.sendChat(Text.translatable("firmament.protectitem").append(event.itemStack.name))
				CommonSoundEffects.playFailure()
			}
			return event.isProtected
		}
	}
}

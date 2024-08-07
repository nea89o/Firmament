
package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

data class SlotClickEvent(
    val slot: Slot,
    val stack: ItemStack,
    val button: Int,
    val actionType: SlotActionType,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<SlotClickEvent>()
}

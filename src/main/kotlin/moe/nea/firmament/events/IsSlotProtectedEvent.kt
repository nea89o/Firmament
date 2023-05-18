package moe.nea.firmament.events

import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC

data class IsSlotProtectedEvent(
    val slot: Slot, var isProtected: Boolean = false
) : FirmamentEvent() {
    companion object : FirmamentEventBus<IsSlotProtectedEvent>() {
        @JvmStatic
        fun shouldBlockInteraction(slot: Slot): Boolean {
            return publish(IsSlotProtectedEvent(slot)).isProtected.also {
                if (it) {
                    MC.player?.sendMessage(Text.translatable("firmament.protectitem").append(slot.stack.name))
                    CommonSoundEffects.playFailure()
                }
            }
        }
    }

    fun protect() {
        isProtected = true
    }
}

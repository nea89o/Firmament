package moe.nea.firmament.events

import net.minecraft.client.option.KeyBinding

data class HandledScreenKeyPressedEvent(val keyCode: Int, val scanCode: Int, val modifiers: Int) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<HandledScreenKeyPressedEvent>()
    fun matches(keyBinding: KeyBinding): Boolean {
        return keyBinding.matchesKey(keyCode, scanCode)
    }
}

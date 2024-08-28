

package moe.nea.firmament.events

import net.minecraft.client.option.KeyBinding
import moe.nea.firmament.keybindings.IKeyBinding

data class WorldKeyboardEvent(val keyCode: Int, val scanCode: Int, val modifiers: Int) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<WorldKeyboardEvent>()

    fun matches(keyBinding: KeyBinding): Boolean {
        return matches(IKeyBinding.minecraft(keyBinding))
    }

    fun matches(keyBinding: IKeyBinding): Boolean {
        return keyBinding.matches(keyCode, scanCode, modifiers)
    }
}

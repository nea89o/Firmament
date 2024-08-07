

package moe.nea.firmament.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.ManagedOption

object FirmamentKeyBindings {
    fun registerKeyBinding(name: String, config: ManagedOption<SavedKeyBinding>) {
        val vanillaKeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                name,
                InputUtil.Type.KEYSYM,
                -1,
                "firmament.key.category"
            )
        )
        keyBindings[vanillaKeyBinding] = config
    }

    val keyBindings = mutableMapOf<KeyBinding, ManagedOption<SavedKeyBinding>>()

}

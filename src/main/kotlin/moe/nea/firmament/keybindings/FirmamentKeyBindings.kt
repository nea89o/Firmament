package moe.nea.firmament.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import org.lwjgl.glfw.GLFW
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object FirmamentKeyBindings {
    val SLOT_LOCKING = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "firmament.key.slotlocking",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "firmament.key.category"
        )
    )
}

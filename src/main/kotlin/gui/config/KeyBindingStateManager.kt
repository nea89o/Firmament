package moe.nea.firmament.gui.config

import org.lwjgl.glfw.GLFW
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.keybindings.SavedKeyBinding

class KeyBindingStateManager(
	val value: () -> SavedKeyBinding,
	val setValue: (key: SavedKeyBinding) -> Unit,
	val blur: () -> Unit,
	val requestFocus: () -> Unit,
) {
	var editing = false
	var lastPressed = 0
	var lastPressedNonModifier = 0
	var label: Text = Text.literal("")

	fun onClick() {
		if (editing) {
			editing = false
			blur()
		} else {
			editing = true
			requestFocus()
		}
		updateLabel()
	}

	fun keyboardEvent(keyCode: Int, pressed: Boolean): Boolean {
		return if (pressed) onKeyPressed(keyCode, SavedKeyBinding.getModInt())
		else onKeyReleased(keyCode, SavedKeyBinding.getModInt())
	}

	fun onKeyPressed(ch: Int, modifiers: Int): Boolean {
		if (!editing) {
			return false
		}
		if (ch == GLFW.GLFW_KEY_ESCAPE) {
			lastPressedNonModifier = 0
			editing = false
			lastPressed = 0
			setValue(SavedKeyBinding(GLFW.GLFW_KEY_UNKNOWN))
			updateLabel()
			blur()
			return true
		}
		if (ch == GLFW.GLFW_KEY_LEFT_SHIFT || ch == GLFW.GLFW_KEY_RIGHT_SHIFT
			|| ch == GLFW.GLFW_KEY_LEFT_ALT || ch == GLFW.GLFW_KEY_RIGHT_ALT
			|| ch == GLFW.GLFW_KEY_LEFT_CONTROL || ch == GLFW.GLFW_KEY_RIGHT_CONTROL
		) {
			lastPressed = ch
		} else {
			setValue(SavedKeyBinding(
				ch, modifiers
			))
			editing = false
			blur()
			lastPressed = 0
			lastPressedNonModifier = 0
		}
		updateLabel()
		return true
	}

	fun onLostFocus() {
		lastPressedNonModifier = 0
		editing = false
		lastPressed = 0
		updateLabel()
	}

	fun onKeyReleased(ch: Int, modifiers: Int): Boolean {
		if (!editing)
			return false
		if (lastPressedNonModifier == ch || (lastPressedNonModifier == 0 && ch == lastPressed)) {
			setValue(SavedKeyBinding(ch, modifiers))
			editing = false
			blur()
			lastPressed = 0
			lastPressedNonModifier = 0
		}
		updateLabel()
		return true
	}

	fun updateLabel() {
		var stroke = value().format()
		if (editing) {
			stroke = Text.literal("")
			val (shift, ctrl, alt) = SavedKeyBinding.getMods(SavedKeyBinding.getModInt())
			if (shift) {
				stroke.append("SHIFT + ")
			}
			if (alt) {
				stroke.append("ALT + ")
			}
			if (ctrl) {
				stroke.append("CTRL + ")
			}
			stroke.append("???")
			stroke.styled { it.withColor(Formatting.YELLOW) }
		}
		label = stroke
	}


}

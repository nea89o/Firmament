package moe.nea.firmament.keybindings

import org.lwjgl.glfw.GLFW
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import moe.nea.firmament.util.MC

// TODO: add support for mouse keybindings
@Serializable
data class SavedKeyBinding(
	val keyCode: Int,
	val shift: Boolean = false,
	val ctrl: Boolean = false,
	val alt: Boolean = false,
) : IKeyBinding {
	val isBound: Boolean get() = keyCode != GLFW.GLFW_KEY_UNKNOWN

	constructor(keyCode: Int, mods: Triple<Boolean, Boolean, Boolean>) : this(
		keyCode,
		mods.first && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT,
		mods.second && keyCode != GLFW.GLFW_KEY_LEFT_CONTROL && keyCode != GLFW.GLFW_KEY_RIGHT_CONTROL,
		mods.third && keyCode != GLFW.GLFW_KEY_LEFT_ALT && keyCode != GLFW.GLFW_KEY_RIGHT_ALT,
	)

	constructor(keyCode: Int, mods: Int) : this(keyCode, getMods(mods))

	companion object {
		fun getMods(modifiers: Int): Triple<Boolean, Boolean, Boolean> {
			return Triple(
				modifiers and GLFW.GLFW_MOD_SHIFT != 0,
				modifiers and GLFW.GLFW_MOD_CONTROL != 0,
				modifiers and GLFW.GLFW_MOD_ALT != 0,
			)
		}

		fun getModInt(): Int {
			val h = MC.window.handle
			val ctrl = if (MinecraftClient.IS_SYSTEM_MAC) {
				InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_SUPER)
					|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_SUPER)
			} else InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_CONTROL)
				|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_CONTROL)
			val shift = isShiftDown()
			val alt = InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_LEFT_ALT)
				|| InputUtil.isKeyPressed(h, GLFW.GLFW_KEY_RIGHT_ALT)
			var mods = 0
			if (ctrl) mods = mods or GLFW.GLFW_MOD_CONTROL
			if (shift) mods = mods or GLFW.GLFW_MOD_SHIFT
			if (alt) mods = mods or GLFW.GLFW_MOD_ALT
			return mods
		}

		private val h get() = MC.window.handle
		fun isShiftDown() = shiftKeys.any { InputUtil.isKeyPressed(h, it) }

		fun unbound(): SavedKeyBinding =
			SavedKeyBinding(GLFW.GLFW_KEY_UNKNOWN)

		val controlKeys = if (MinecraftClient.IS_SYSTEM_MAC) {
			listOf(GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)
		} else {
			listOf(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)
		}
		val shiftKeys = listOf(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT)

		val altKeys = listOf(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT)
	}

	fun isPressed(atLeast: Boolean = false): Boolean {
		if (!isBound) return false
		val h = MC.window.handle
		if (!InputUtil.isKeyPressed(h, keyCode)) return false

		// These are modifiers, so if the searched keyCode is a modifier key, then that key does not count as the modifier
		val ctrl = keyCode !in controlKeys && controlKeys.any { InputUtil.isKeyPressed(h, it) }
		val shift = keyCode !in shiftKeys && isShiftDown()
		val alt = keyCode !in altKeys && altKeys.any { InputUtil.isKeyPressed(h, it) }
		if (atLeast)
			return (ctrl >= this.ctrl) &&
				(alt >= this.alt) &&
				(shift >= this.shift)

		return (ctrl == this.ctrl) &&
			(alt == this.alt) &&
			(shift == this.shift)
	}

	override fun matchesAtLeast(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (this.keyCode == GLFW.GLFW_KEY_UNKNOWN) return false
		val (shift, ctrl, alt) = getMods(modifiers)
		return keyCode == this.keyCode && this.shift <= shift && this.ctrl <= ctrl && this.alt <= alt
	}

	override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (this.keyCode == GLFW.GLFW_KEY_UNKNOWN) return false
		return keyCode == this.keyCode && getMods(modifiers) == Triple(shift, ctrl, alt)
	}

	override fun toString(): String {
		return format().string
	}

	fun format(): Text {
		val stroke = Text.literal("")
		if (ctrl) {
			stroke.append("CTRL + ")
		}
		if (alt) {
			stroke.append("ALT + ")
		}
		if (shift) {
			stroke.append("SHIFT + ") // TODO: translations?
		}

		stroke.append(InputUtil.Type.KEYSYM.createFromCode(keyCode).localizedText)
		return stroke
	}

}

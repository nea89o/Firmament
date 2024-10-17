package moe.nea.firmament.events

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.option.KeyBinding
import moe.nea.firmament.keybindings.IKeyBinding

sealed interface HandledScreenKeyEvent {
	val screen: HandledScreen<*>
	val keyCode: Int
	val scanCode: Int
	val modifiers: Int

	fun matches(keyBinding: KeyBinding): Boolean {
		return matches(IKeyBinding.minecraft(keyBinding))
	}

	fun matches(keyBinding: IKeyBinding): Boolean {
		return keyBinding.matches(keyCode, scanCode, modifiers)
	}
}

data class HandledScreenKeyPressedEvent(
	override val screen: HandledScreen<*>,
	override val keyCode: Int,
	override val scanCode: Int,
	override val modifiers: Int
) : FirmamentEvent.Cancellable(), HandledScreenKeyEvent {
	companion object : FirmamentEventBus<HandledScreenKeyPressedEvent>()
}

data class HandledScreenKeyReleasedEvent(
	override val screen: HandledScreen<*>,
	override val keyCode: Int,
	override val scanCode: Int,
	override val modifiers: Int
) : FirmamentEvent.Cancellable(), HandledScreenKeyEvent {
	companion object : FirmamentEventBus<HandledScreenKeyReleasedEvent>()
}

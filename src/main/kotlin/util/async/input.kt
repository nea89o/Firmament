package moe.nea.firmament.util.async

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.component.CenterComponent
import io.github.notenoughupdates.moulconfig.gui.component.ColumnComponent
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.keybindings.IKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil

private object InputHandler {
	data class KeyInputContinuation(val keybind: IKeyBinding, val onContinue: () -> Unit)

	private val activeContinuations = mutableListOf<KeyInputContinuation>()

	fun registerContinuation(keyInputContinuation: KeyInputContinuation): () -> Unit {
		synchronized(InputHandler) {
			activeContinuations.add(keyInputContinuation)
		}
		return {
			synchronized(this) {
				activeContinuations.remove(keyInputContinuation)
			}
		}
	}

	init {
		HandledScreenKeyPressedEvent.subscribe("Input:resumeAfterInput") { event ->
			synchronized(InputHandler) {
				val toRemove = activeContinuations.filter {
					event.matches(it.keybind)
				}
				toRemove.forEach { it.onContinue() }
				activeContinuations.removeAll(toRemove)
			}
		}
	}
}

suspend fun waitForInput(keybind: IKeyBinding): Unit = suspendCancellableCoroutine { cont ->
	val unregister =
		InputHandler.registerContinuation(InputHandler.KeyInputContinuation(keybind) { cont.resume(Unit) })
	cont.invokeOnCancellation {
		unregister()
	}
}


fun createPromptScreenGuiComponent(suggestion: String, prompt: String, action: Runnable) = (run {
	val text = GetSetter.floating(suggestion)
	GuiContext(
		CenterComponent(
			PanelComponent(
				ColumnComponent(
					TextFieldComponent(text, 120),
					FirmButtonComponent(TextComponent(prompt), action = action)
				)
			)
		)
	) to text
})

suspend fun waitForTextInput(suggestion: String, prompt: String) =
	suspendCancellableCoroutine<String> { cont ->
		lateinit var screen: Screen
		lateinit var text: GetSetter<String>
		val action = {
			if (MC.screen === screen)
				MC.screen = null
			// TODO: should this exit
			cont.resume(text.get())
		}
		val (gui, text_) = createPromptScreenGuiComponent(suggestion, prompt, action)
		text = text_
		screen = MoulConfigUtils.wrapScreen(gui, null, onClose = action)
		ScreenUtil.setScreenLater(screen)
		cont.invokeOnCancellation {
			action()
		}
	}

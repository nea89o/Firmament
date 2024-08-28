

package moe.nea.firmament.util.async

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.keybindings.IKeyBinding

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
        HandledScreenKeyPressedEvent.subscribe { event ->
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



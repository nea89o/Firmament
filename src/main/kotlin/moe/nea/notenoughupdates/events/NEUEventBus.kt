package moe.nea.notenoughupdates.events

import java.util.concurrent.CopyOnWriteArrayList

open class NEUEventBus<T : NEUEvent> {
    data class Handler<T>(val invocation: (T) -> Unit, val receivesCancelled: Boolean)

    private val toHandle: MutableList<Handler<T>> = CopyOnWriteArrayList()
    fun subscribe(handle: (T) -> Unit) {
        subscribe(handle, false)
    }

    fun subscribe(handle: (T) -> Unit, receivesCancelled: Boolean) {
        toHandle.add(Handler(handle, receivesCancelled))
    }

    fun publish(event: T): T {
        for (function in toHandle) {
            if (function.receivesCancelled || event !is NEUEvent.Cancellable || !event.cancelled) {
                function.invocation(event)
            }
        }
        return event
    }

}

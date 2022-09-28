package moe.nea.notenoughupdates.events

import java.util.concurrent.CopyOnWriteArrayList
import moe.nea.notenoughupdates.NotEnoughUpdates

/**
 * A pubsub event bus.
 *
 * [subscribe] to events [publish]ed on this event bus.
 * Subscriptions may not necessarily be delivered in the order or registering.
 */
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
                try {
                    function.invocation(event)
                } catch (e: Exception) {
                    NotEnoughUpdates.logger.error("Caught exception during processing event $event", e)
                }
            }
        }
        return event
    }

}

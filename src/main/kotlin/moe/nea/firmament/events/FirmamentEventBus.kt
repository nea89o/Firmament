

package moe.nea.firmament.events

import java.util.concurrent.CopyOnWriteArrayList
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MC

/**
 * A pubsub event bus.
 *
 * [subscribe] to events [publish]ed on this event bus.
 * Subscriptions may not necessarily be delivered in the order of registering.
 */
open class FirmamentEventBus<T : FirmamentEvent> {
    data class Handler<T>(
        val invocation: (T) -> Unit, val receivesCancelled: Boolean,
        var knownErrors: MutableSet<Class<*>> = mutableSetOf(),
    )

    private val toHandle: MutableList<Handler<T>> = CopyOnWriteArrayList()
    fun subscribe(handle: (T) -> Unit) {
        subscribe(false, handle)
    }

    fun subscribe(receivesCancelled: Boolean, handle: (T) -> Unit) {
        toHandle.add(Handler(handle, receivesCancelled))
    }

    fun publish(event: T): T {
        for (function in toHandle) {
            if (function.receivesCancelled || event !is FirmamentEvent.Cancellable || !event.cancelled) {
                try {
                    function.invocation(event)
                } catch (e: Exception) {
                    val klass = e.javaClass
                    if (!function.knownErrors.contains(klass) || Firmament.DEBUG) {
                        function.knownErrors.add(klass)
                        Firmament.logger.error("Caught exception during processing event $event by $function", e)
                    }
                }
            }
        }
        return event
    }

    fun publishSync(event: T) {
        MC.onMainThread {
            publish(event)
        }
    }
}

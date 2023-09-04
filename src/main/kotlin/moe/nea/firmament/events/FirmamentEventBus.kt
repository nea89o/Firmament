/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import java.util.concurrent.CopyOnWriteArrayList
import moe.nea.firmament.Firmament

/**
 * A pubsub event bus.
 *
 * [subscribe] to events [publish]ed on this event bus.
 * Subscriptions may not necessarily be delivered in the order of registering.
 */
open class FirmamentEventBus<T : FirmamentEvent> {
    data class Handler<T>(val invocation: (T) -> Unit, val receivesCancelled: Boolean)

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
                    Firmament.logger.error("Caught exception during processing event $event by $function", e)
                }
            }
        }
        return event
    }

}

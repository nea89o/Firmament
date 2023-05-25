/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.events

import java.util.concurrent.CopyOnWriteArrayList
import moe.nea.firmament.Firmament

/**
 * A pubsub event bus.
 *
 * [subscribe] to events [publish]ed on this event bus.
 * Subscriptions may not necessarily be delivered in the order or registering.
 */
open class FirmamentEventBus<T : FirmamentEvent> {
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
            if (function.receivesCancelled || event !is FirmamentEvent.Cancellable || !event.cancelled) {
                try {
                    function.invocation(event)
                } catch (e: Exception) {
                    Firmament.logger.error("Caught exception during processing event $event", e)
                }
            }
        }
        return event
    }

}

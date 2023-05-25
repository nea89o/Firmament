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

/**
 * An event that can be fired by a [FirmamentEventBus].
 *
 * Typically, that event bus is implemented as a companion object
 *
 * ```
 * class SomeEvent : NEUEvent() {
 *     companion object : NEUEventBus<SomeEvent>()
 * }
 * ```
 */
abstract class FirmamentEvent {
    /**
     * A [FirmamentEvent] that can be [cancelled]
     */
    abstract class Cancellable : FirmamentEvent() {
        /**
         * Cancels this is event.
         *
         * @see cancelled
         */
        fun cancel() {
            cancelled = true
        }

        /**
         * Whether this event is cancelled.
         *
         * Cancelled events will bypass handlers unless otherwise specified and will prevent the action that this
         * event was originally fired for.
         */
        var cancelled: Boolean = false
    }
}

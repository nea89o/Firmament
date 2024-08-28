

package moe.nea.firmament.events

/**
 * An event that can be fired by a [FirmamentEventBus].
 *
 * Typically, that event bus is implemented as a companion object
 *
 * ```
 * class SomeEvent : FirmamentEvent() {
 *     companion object : FirmamentEventBus<SomeEvent>()
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

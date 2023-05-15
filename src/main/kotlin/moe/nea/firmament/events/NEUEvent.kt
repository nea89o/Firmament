package moe.nea.firmament.events

/**
 * An event that can be fired by a [NEUEventBus].
 *
 * Typically, that event bus is implemented as a companion object
 *
 * ```
 * class SomeEvent : NEUEvent() {
 *     companion object : NEUEventBus<SomeEvent>()
 * }
 * ```
 */
abstract class NEUEvent {
    /**
     * A [NEUEvent] that can be [cancelled]
     */
    abstract class Cancellable : NEUEvent() {
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

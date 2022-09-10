package moe.nea.notenoughupdates.events

abstract class NEUEvent {
    abstract class Cancellable : NEUEvent() {
        var cancelled: Boolean = false
    }
}

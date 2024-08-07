

package moe.nea.firmament.features.debug

import moe.nea.firmament.Firmament
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.TimeMark

object DebugView : FirmamentFeature {
    private data class StoredVariable<T>(
        val obj: T,
        val timer: TimeMark,
    )

    private val storedVariables: MutableMap<String, StoredVariable<*>> = sortedMapOf()
    override val identifier: String
        get() = "debug-view"
    override val defaultEnabled: Boolean
        get() = Firmament.DEBUG

    fun <T : Any?> showVariable(label: String, obj: T) {
        synchronized(this) {
            storedVariables[label] = StoredVariable(obj, TimeMark.now())
        }
    }

    fun recalculateDebugWidget() {
    }

    override fun onLoad() {
        TickEvent.subscribe {
            synchronized(this) {
                recalculateDebugWidget()
            }
        }
    }
}

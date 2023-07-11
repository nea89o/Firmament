package moe.nea.firmament.features.debug

import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.data.Axis
import java.util.Optional
import kotlin.time.Duration.Companion.seconds
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.Team
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark

object DebugView : FirmamentFeature {
    private data class StoredVariable<T>(
        val obj: T,
        val timer: TimeMark,
    )

    private val storedVariables: MutableMap<String, StoredVariable<*>> = mutableMapOf()
    override val identifier: String
        get() = "debug-view"
    override val defaultEnabled: Boolean
        get() = Firmament.DEBUG

    fun <T : Any?> showVariable(label: String, obj: T) {
        synchronized(this) {
            storedVariables[label] = StoredVariable(obj, TimeMark.now())
        }
    }

    val debugWidget = WBox(Axis.VERTICAL)


    override fun onLoad() {
        TickEvent.subscribe {
            synchronized(this) {
                storedVariables.entries.removeIf { it.value.timer.passedTime() > 1.seconds }
                if (storedVariables.isEmpty()) {
                    CottonHud.add(debugWidget, 20, 20)
                } else {
                    CottonHud.remove(debugWidget)
                }
            }
        }
    }
}

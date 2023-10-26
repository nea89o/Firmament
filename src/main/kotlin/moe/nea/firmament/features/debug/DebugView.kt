/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WDynamicLabel
import io.github.cottonmc.cotton.gui.widget.data.Axis
import java.util.stream.Collectors
import kotlin.time.Duration.Companion.seconds
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
        storedVariables.entries.removeIf { it.value.timer.passedTime() > 1.seconds }
        debugWidget.streamChildren().collect(Collectors.toList()).forEach {
            debugWidget.remove(it)
        }
        storedVariables.entries.forEach {
            debugWidget.add(WDynamicLabel({ "${it.key}: ${it.value.obj}" }))
        }
        debugWidget.layout()
        if (storedVariables.isNotEmpty()) {
            CottonHud.add(debugWidget, 20, 20)
        } else {
            CottonHud.remove(debugWidget)
        }
    }

    val debugWidget = WBox(Axis.VERTICAL)


    override fun onLoad() {
        TickEvent.subscribe {
            synchronized(this) {
                recalculateDebugWidget()
            }
        }
    }
}

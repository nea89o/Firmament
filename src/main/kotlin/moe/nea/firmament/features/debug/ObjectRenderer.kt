/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty1
import net.minecraft.text.Text
import moe.nea.firmament.gui.WSpacer

class ObjectRenderer(val box: WBox) {
    var indent = 0

    fun beginObject() {
        indent++
    }

    fun endObject() {
        indent--
    }

    fun emit(label: String, widget: WWidget) {
        WSpacer(WBox(Axis.VERTICAL).also {
            it.add(WWidget())
            it.add(widget)
        }, indent * 18)
    }

    fun <T : Any?> getDebuggingView(label: String, obj: T) {
        if (obj == null) {
            emit(label, WLabel(Text.literal("§cnull")))
            return
        }
        if (obj is String) {
            emit(label, WLabel(Text.literal(Json.encodeToString(obj))))
        }
        getObject(label, obj)
    }

    fun <T : Any> getObject(label: String, obj: T) {
        emit(label, WLabel(Text.literal(obj::class.simpleName ?: "<unknown>")))
        beginObject()
        for (prop in obj::class.members.filterIsInstance<KProperty1<T, *>>()) {
            val child = prop.get(obj)
            getDebuggingView(prop.name, child)
        }
        endObject()
    }

}

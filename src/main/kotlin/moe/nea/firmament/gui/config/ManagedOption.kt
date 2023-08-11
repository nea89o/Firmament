/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import net.minecraft.text.Text
import moe.nea.firmament.Firmament

class ManagedOption<T : Any>(
    val element: ManagedConfigElement,
    val propertyName: String,
    val default: () -> T,
    val handler: ManagedConfig.OptionHandler<T>
) : ReadWriteProperty<Any?, T> {

    val rawLabelText = "firmament.config.${element.name}.${propertyName}"
    val labelText = Text.translatable(rawLabelText)

    lateinit var value: T

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    fun load(root: JsonElement) {
        if (root is JsonObject && root.containsKey(propertyName)) {
            try {
                value = handler.fromJson(root[propertyName]!!)
                return
            } catch (e: Exception) {
                Firmament.logger.error(
                    "Exception during loading of config file ${element.name}. This will reset this config.",
                    e
                )
            }
        }
        value = default()
    }

    fun toJson(): JsonElement? {
        return handler.toJson(value)
    }

    fun appendToGui(guiapp: GuiAppender) {
        handler.emitGuiElements(this, guiapp)
    }
}

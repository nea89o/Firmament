/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.component.SwitchComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

class BooleanHandler(val config: ManagedConfig) : ManagedConfig.OptionHandler<Boolean> {
    override fun toJson(element: Boolean): JsonElement? {
        return JsonPrimitive(element)
    }

    override fun fromJson(element: JsonElement): Boolean {
        return element.jsonPrimitive.boolean
    }

    override fun emitGuiElements(opt: ManagedOption<Boolean>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            SwitchComponent(object : GetSetter<Boolean> {
                override fun get(): Boolean {
                    return opt.get()
                }

                override fun set(newValue: Boolean) {
                    opt.set(newValue)
                    config.save()
                }
            }, 200)
        )
    }
}

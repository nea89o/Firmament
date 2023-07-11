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

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WToggleButton
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

    override fun emitGuiElements(opt: ManagedConfig.Option<Boolean>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            WToggleButton(opt.labelText).apply {
                guiAppender.onReload { toggle = opt.value }
                setOnToggle {
                    opt.value = it
                    config.save()
                }
            }
        )
    }
}

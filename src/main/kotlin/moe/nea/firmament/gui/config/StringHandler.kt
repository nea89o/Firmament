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

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WTextField
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.text.Text

class StringHandler(val config: ManagedConfig) : ManagedConfig.OptionHandler<String> {
    override fun toJson(element: String): JsonElement? {
        return JsonPrimitive(element)
    }

    override fun fromJson(element: JsonElement): String {
        return element.jsonPrimitive.content
    }

    override fun emitGuiElements(opt: ManagedConfig.Option<String>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            WTextField(opt.labelText).apply {
                maxLength = 1000
                suggestion = Text.translatableWithFallback(opt.rawLabelText + ".hint", "")
                guiAppender.onReload { text = opt.value }
                setChangedListener {
                    opt.value = it
                    config.save()
                }
            }
        )
    }
}

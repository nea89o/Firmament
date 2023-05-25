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

import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import kotlinx.serialization.json.JsonElement
import net.minecraft.text.Text

class ClickHandler(val config: ManagedConfig, val runnable: () -> Unit) : ManagedConfig.OptionHandler<Unit> {
    override fun toJson(element: Unit): JsonElement? {
        return null
    }

    override fun fromJson(element: JsonElement) {}

    override fun emitGuiElements(opt: ManagedConfig.Option<Unit>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            Text.translatable("firmament.config.${config.name}.${opt.propertyName}"),
            WButton(Text.translatable("firmament.config.${config.name}.${opt.propertyName}")).apply {
                setOnClick {
                    runnable()
                }
            },
        )
    }
}

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

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

    override fun emitGuiElements(opt: ManagedOption<String>, guiAppender: GuiAppender) {
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

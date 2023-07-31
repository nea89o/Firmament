/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import java.util.function.IntConsumer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.milliseconds
import net.minecraft.text.Text
import moe.nea.firmament.util.FirmFormatters

class IntegerHandler(val config: ManagedConfig, val min: Int, val max: Int) : ManagedConfig.OptionHandler<Int> {
    override fun toJson(element: Int): JsonElement? {
        return JsonPrimitive(element)
    }

    override fun fromJson(element: JsonElement): Int {
        return element.jsonPrimitive.int
    }

    override fun emitGuiElements(opt: ManagedConfig.Option<Int>, guiAppender: GuiAppender) {
        val label =
            WLabel(Text.literal(opt.value.toString())).setVerticalAlignment(VerticalAlignment.CENTER)
        guiAppender.appendLabeledRow(opt.labelText, WBox(Axis.HORIZONTAL).also {
            it.add(label, 40, 18)
            it.add(WSlider(min, max, Axis.HORIZONTAL).apply {
                valueChangeListener = IntConsumer {
                    opt.value = it
                    label.text = Text.literal(opt.value.toString())
                    config.save()
                }
                guiAppender.onReload {
                    value = opt.value
                    label.text = Text.literal(opt.value.toString())
                }
            }, 130, 18)
        })
    }

}

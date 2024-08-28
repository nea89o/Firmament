

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import moe.nea.firmament.util.FirmFormatters

class IntegerHandler(val config: ManagedConfig, val min: Int, val max: Int) : ManagedConfig.OptionHandler<Int> {
    override fun toJson(element: Int): JsonElement? {
        return JsonPrimitive(element)
    }

    override fun fromJson(element: JsonElement): Int {
        return element.jsonPrimitive.int
    }

    override fun emitGuiElements(opt: ManagedOption<Int>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            RowComponent(
                TextComponent(IMinecraft.instance.defaultFontRenderer,
                              { FirmFormatters.formatCommas(opt.value, 0) },
                              40,
                              TextComponent.TextAlignment.CENTER,
                              true,
                              false),
                SliderComponent(
                    object : GetSetter<Float> {
                        override fun get(): Float {
                            return opt.value.toFloat()
                        }

                        override fun set(newValue: Float) {
                            opt.value = newValue.toInt()
                        }
                    },
                    min.toFloat(),
                    max.toFloat(),
                    0.1F,
                    130
                )
            ))

    }

}

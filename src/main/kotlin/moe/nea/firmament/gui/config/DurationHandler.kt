

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import net.minecraft.text.Text
import moe.nea.firmament.util.FirmFormatters

class DurationHandler(val config: ManagedConfig, val min: Duration, val max: Duration) :
    ManagedConfig.OptionHandler<Duration> {
    override fun toJson(element: Duration): JsonElement? {
        return JsonPrimitive(element.inWholeMilliseconds)
    }

    override fun fromJson(element: JsonElement): Duration {
        return element.jsonPrimitive.long.toDuration(DurationUnit.MILLISECONDS)
    }

    override fun emitGuiElements(opt: ManagedOption<Duration>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            RowComponent(
                TextComponent(IMinecraft.instance.defaultFontRenderer,
                              { FirmFormatters.formatTimespan(opt.value) },
                              40,
                              TextComponent.TextAlignment.CENTER,
                              true,
                              false),
                SliderComponent(
                    object : GetSetter<Float> {
                        override fun get(): Float {
                            return opt.value.toDouble(DurationUnit.SECONDS).toFloat()
                        }

                        override fun set(newValue: Float) {
                            opt.value = newValue.toDouble().toDuration(DurationUnit.SECONDS)
                        }
                    },
                    min.toDouble(DurationUnit.SECONDS).toFloat(),
                    max.toDouble(DurationUnit.SECONDS).toFloat(),
                    0.1F,
                    130
                )
            ))
    }

}

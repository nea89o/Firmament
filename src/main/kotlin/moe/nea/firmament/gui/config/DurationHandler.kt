package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import java.util.function.IntConsumer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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

    override fun emitGuiElements(opt: ManagedConfig.Option<Duration>, guiAppender: GuiAppender) {
        val lw = guiAppender.width / 2
        guiAppender.set(
            0, 0, lw, 1,
            WLabel(opt.labelText).setVerticalAlignment(VerticalAlignment.CENTER)
        )
        val label =
            WLabel(Text.literal(FirmFormatters.formatTimespan(opt.value))).setVerticalAlignment(VerticalAlignment.CENTER)
        guiAppender.set(lw, 0, 2, 1, label)
        guiAppender.set(
            lw + 2,
            0,
            lw - 2,
            1,
            WSlider(min.inWholeMilliseconds.toInt(), max.inWholeMilliseconds.toInt(), Axis.HORIZONTAL).apply {
                valueChangeListener = IntConsumer {
                    opt.value = it.milliseconds
                    label.text = Text.literal(FirmFormatters.formatTimespan(opt.value))
                    config.save()
                }
                guiAppender.onReload {
                    value = opt.value.inWholeMilliseconds.toInt()
                    label.text = Text.literal(FirmFormatters.formatTimespan(opt.value))
                }
            })
        guiAppender.skipRows(1)
    }

}

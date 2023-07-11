package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import java.util.function.IntConsumer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.text.Text

class IntegerHandler(val config: ManagedConfig, val min: Int, val max: Int) : ManagedConfig.OptionHandler<Int> {
    override fun toJson(element: Int): JsonElement? {
        return JsonPrimitive(element)
    }

    override fun fromJson(element: JsonElement): Int {
        return element.jsonPrimitive.int
    }

    override fun emitGuiElements(opt: ManagedConfig.Option<Int>, guiAppender: GuiAppender) {
        val lw = guiAppender.width / 2
        guiAppender.set(
            0, 0, lw, 1,
            WLabel(opt.labelText).setVerticalAlignment(VerticalAlignment.CENTER)
        )
        val label =
            WLabel(Text.literal(opt.value.toString())).setVerticalAlignment(VerticalAlignment.CENTER)
        guiAppender.set(lw, 0, 2, 1, label)
        guiAppender.set(
            lw + 2,
            0,
            lw - 2,
            1,
            WSlider(min, max, Axis.HORIZONTAL).apply {
                valueChangeListener = IntConsumer {
                    opt.value = it
                    label.text = Text.literal(opt.value.toString())
                    config.save()
                }
                guiAppender.onReload {
                    value = opt.value
                    label.text = Text.literal(opt.value.toString())
                }
            })
        guiAppender.skipRows(1)
    }

}

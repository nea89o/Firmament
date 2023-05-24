package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.text.Text

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

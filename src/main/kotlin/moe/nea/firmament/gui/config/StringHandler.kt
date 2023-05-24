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
        return element.jsonPrimitive.toString()
    }

    override fun emitGuiElements(opt: ManagedConfig.Option<String>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            WTextField(opt.labelText).apply {
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



package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
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
            TextFieldComponent(
                object : GetSetter<String> by opt {
                    override fun set(newValue: String) {
                        opt.set(newValue)
                        config.save()
                    }
                },
                130,
                suggestion = Text.translatableWithFallback(opt.rawLabelText + ".hint", "").string
            ),
        )
    }
}

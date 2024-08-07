

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import kotlinx.serialization.json.JsonElement
import moe.nea.firmament.gui.FirmButtonComponent

class ClickHandler(val config: ManagedConfig, val runnable: () -> Unit) : ManagedConfig.OptionHandler<Unit> {
    override fun toJson(element: Unit): JsonElement? {
        return null
    }

    override fun fromJson(element: JsonElement) {}

    override fun emitGuiElements(opt: ManagedOption<Unit>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            FirmButtonComponent(
                TextComponent(opt.labelText.string),
                action = runnable),
        )
    }
}

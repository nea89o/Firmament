package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import kotlinx.serialization.json.JsonElement
import net.minecraft.text.Text

class ClickHandler(val config: ManagedConfig, val runnable: () -> Unit) : ManagedConfig.OptionHandler<Unit> {
    override fun toJson(element: Unit): JsonElement? {
        return null
    }

    override fun fromJson(element: JsonElement) {}

    override fun emitGuiElements(opt: ManagedConfig.Option<Unit>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            Text.translatable("firmament.config.${config.name}.${opt.propertyName}"),
            WButton(Text.translatable("firmament.config.${config.name}.${opt.propertyName}")).apply {
                setOnClick {
                    runnable()
                }
            },
        )
    }
}

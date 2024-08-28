

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.jarvis.JarvisIntegration
import moe.nea.firmament.util.MC

class HudMetaHandler(val config: ManagedConfig, val label: MutableText, val width: Int, val height: Int) :
    ManagedConfig.OptionHandler<HudMeta> {
    override fun toJson(element: HudMeta): JsonElement? {
        return Json.encodeToJsonElement(element.position)
    }

    override fun fromJson(element: JsonElement): HudMeta {
        return HudMeta(Json.decodeFromJsonElement(element), label, width, height)
    }

    override fun emitGuiElements(opt: ManagedOption<HudMeta>, guiAppender: GuiAppender) {
        guiAppender.appendLabeledRow(
            opt.labelText,
            FirmButtonComponent(
                TextComponent(
                    Text.stringifiedTranslatable("firmament.hud.edit", label).string),
            ) {
                MC.screen = JarvisIntegration.jarvis.getHudEditor(
                    guiAppender.screenAccessor.invoke(),
                    listOf(opt.value)
                )
            })
    }
}

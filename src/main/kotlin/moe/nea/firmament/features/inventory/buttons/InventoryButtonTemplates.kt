

package moe.nea.firmament.features.inventory.buttons

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TemplateUtil

object InventoryButtonTemplates {

    val legacyPrefix = "NEUBUTTONS/"
    val modernPrefix = "MAYBEONEDAYIWILLHAVEMYOWNFORMAT"

    fun loadTemplate(t: String): List<InventoryButton>? {
        val buttons = TemplateUtil.maybeDecodeTemplate<List<String>>(legacyPrefix, t) ?: return null
        return buttons.mapNotNull {
            try {
                Firmament.json.decodeFromString<InventoryButton>(it).also {
                    if (it.icon?.startsWith("extra:") == true || it.command?.any { it.isLowerCase() } == true) {
                        MC.sendChat(Text.translatable("firmament.inventory-buttons.import-failed"))
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun saveTemplate(buttons: List<InventoryButton>): String {
        return TemplateUtil.encodeTemplate(legacyPrefix, buttons.map { Firmament.json.encodeToString(it) })
    }
}

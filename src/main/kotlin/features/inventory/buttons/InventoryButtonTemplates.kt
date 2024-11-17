package moe.nea.firmament.features.inventory.buttons

import kotlinx.serialization.encodeToString
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TemplateUtil

object InventoryButtonTemplates {

	val legacyPrefix = "NEUBUTTONS/"
	val modernPrefix = "MAYBEONEDAYIWILLHAVEMYOWNFORMAT"

	fun loadTemplate(t: String): List<InventoryButton>? {
		val buttons = TemplateUtil.maybeDecodeTemplate<List<String>>(legacyPrefix, t) ?: return null
		return buttons.mapNotNull {
			ErrorUtil.catch<InventoryButton?>("Could not import button") {
				Firmament.json.decodeFromString<InventoryButton>(it).also {
					if (it.icon?.startsWith("extra:") == true) {
						MC.sendChat(Text.translatable("firmament.inventory-buttons.import-failed"))
					}
				}
			}.or {
				null
			}
		}
	}

	fun saveTemplate(buttons: List<InventoryButton>): String {
		return TemplateUtil.encodeTemplate(legacyPrefix, buttons.map { Firmament.json.encodeToString(it) })
	}
}

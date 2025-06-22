package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.ScreenUtil.getSlotByIndex
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr

object ExportRecipe {


	val xNames = "ABC"
	val yNames = "123"

	val slotIndices = (0..<9).map {
		val x = it % 3
		val y = it / 3

		(xNames[x].toString() + yNames[y]) to x + y * 9 + 10
	}
	val resultSlot = 25

	@Subscribe
	fun onRecipeKeyBind(event: HandledScreenKeyPressedEvent) {
		if (!event.matches(PowerUserTools.TConfig.exportUIRecipes)) {
			return
		}
		if (!event.screen.title.string.endsWith(" Recipe")) {
			MC.sendChat(tr("firmament.repo.export.recipe.fail", "No Recipe found"))
			return
		}
		slotIndices.forEach { (_, index) ->
			event.screen.getSlotByIndex(index, false)?.stack?.let(ItemExporter::ensureExported)
		}
		val inputs = slotIndices.associate { (name, index) ->
			val id = event.screen.getSlotByIndex(index, false)?.stack?.takeIf { !it.isEmpty() }?.let {
				"${it.skyBlockId?.neuItem}:${it.count}"
			} ?: ""
			name to JsonPrimitive(id)
		}
		val output = event.screen.getSlotByIndex(resultSlot, false)?.stack!!
		val overrideOutputId = output.skyBlockId!!.neuItem
		val count = output.count
		val recipe = JsonObject(
			inputs + mapOf(
				"type" to JsonPrimitive("crafting"),
				"count" to JsonPrimitive(count),
				"overrideOutputId" to JsonPrimitive(overrideOutputId)
			)
		)
		ItemExporter.appendRecipe(output.skyBlockId!!, recipe)
	}
}

package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.repo.RepoDownloadManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.setSkyBlockId
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr

object ItemExporter {

	fun exportItem(itemStack: ItemStack): Text {
		val exporter = LegacyItemExporter.createExporter(itemStack)
		val json = exporter.exportJson()
		val jsonFormatted = Firmament.twoSpaceJson.encodeToString(json)
		val fileName = json.jsonObject["internalname"]!!.jsonPrimitive.content
		val itemFile = RepoDownloadManager.repoSavedLocation.resolve("items").resolve("${fileName}.json")
		itemFile.createParentDirectories()
		itemFile.writeText(jsonFormatted)
		val overlayFile = RepoDownloadManager.repoSavedLocation.resolve("itemsOverlay")
			.resolve(ExportedTestConstantMeta.current.dataVersion.toString())
			.resolve("${fileName}.snbt")
		overlayFile.createParentDirectories()
		overlayFile.writeText(exporter.exportModernSnbt().toPrettyString())
		return tr(
			"firmament.repoexport.success",
			"Exported item to ${itemFile.relativeTo(RepoDownloadManager.repoSavedLocation)}${
				exporter.warnings.joinToString(
					""
				) { "\nWarning: $it" }
			}"
		)
	}

	fun pathFor(skyBlockId: SkyblockId) =
		RepoManager.neuRepo.baseFolder.resolve("items/${skyBlockId.neuItem}.json")

	fun isExported(skyblockId: SkyblockId) =
		pathFor(skyblockId).exists()

	fun ensureExported(itemStack: ItemStack) {
		if (!isExported(itemStack.skyBlockId ?: return))
			exportItem(itemStack)
	}

	fun modifyJson(skyblockId: SkyblockId, modify: (JsonObject) -> JsonObject) {
		val oldJson = Firmament.json.decodeFromString<JsonObject>(pathFor(skyblockId).readText())
		val newJson = modify(oldJson)
		pathFor(skyblockId).writeText(Firmament.twoSpaceJson.encodeToString(JsonObject(newJson)))
	}

	fun appendRecipe(skyblockId: SkyblockId, recipe: JsonObject) {
		modifyJson(skyblockId) { oldJson ->
			val mutableJson = oldJson.toMutableMap()
			val recipes = ((mutableJson["recipes"] as JsonArray?) ?: listOf()).toMutableList()
			recipes.add(recipe)
			mutableJson["recipes"] = JsonArray(recipes)
			JsonObject(mutableJson)
		}
	}

	@Subscribe
	fun onKeyBind(event: HandledScreenKeyPressedEvent) {
		if (event.matches(PowerUserTools.TConfig.exportItemStackToRepo)) {
			val itemStack = event.screen.focusedItemStack ?: return
			PowerUserTools.lastCopiedStack = (itemStack to exportItem(itemStack))
		}
	}

	fun exportStub(skyblockId: SkyblockId, title: String, extra: (ItemStack) -> Unit = {}) {
		exportItem(ItemStack(Items.PLAYER_HEAD).also {
			it.displayNameAccordingToNbt = Text.literal(title)
			it.loreAccordingToNbt = listOf(Text.literal(""))
			it.setSkyBlockId(skyblockId)
			extra(it) // LOL
		})
		MC.sendChat(tr("firmament.repo.export.stub", "Exported a stub item for $skyblockId"))
	}
}

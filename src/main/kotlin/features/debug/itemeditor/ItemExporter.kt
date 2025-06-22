package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.path.createParentDirectories
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.repo.RepoDownloadManager
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString
import moe.nea.firmament.util.tr

object ItemExporter {


	@Subscribe
	fun onKeyBind(event: HandledScreenKeyPressedEvent) {
		if (event.matches(PowerUserTools.TConfig.exportItemStackToRepo)) {
			val itemStack = event.screen.focusedItemStack ?: return
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

			PowerUserTools.lastCopiedStack = Pair(
				itemStack,
				tr(
					"firmament.repoexport.success",
					"Exported item to ${itemFile.relativeTo(RepoDownloadManager.repoSavedLocation)}${
						exporter.warnings.joinToString(
							""
						) { "\nWarning: $it" }
					}"
				)
			)
		}
	}
}

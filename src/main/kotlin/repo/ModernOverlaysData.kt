package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import moe.nea.firmament.util.SkyblockId

// TODO: move this over to the repo parser
class ModernOverlaysData : IReloadable {
	data class OverlayFile(
		val version: Int,
		val path: Path,
	)

	var overlays: Map<SkyblockId, List<OverlayFile>> = mapOf()
	override fun reload(repo: NEURepository) {
		val items = mutableMapOf<SkyblockId, MutableList<OverlayFile>>()
		repo.baseFolder.resolve("itemsOverlay")
			.listDirectoryEntries()
			.forEach { versionFolder ->
				val version = versionFolder.fileName.toString().toIntOrNull() ?: return@forEach
				versionFolder.listDirectoryEntries()
					.forEach { item ->
						if (item.extension != "snbt") return@forEach
						val itemId = item.nameWithoutExtension
						items.getOrPut(SkyblockId(itemId)) { mutableListOf() }.add(OverlayFile(version, item))
					}
			}
		this.overlays = items
	}

	fun getOverlayFiles(skyblockId: SkyblockId) = overlays[skyblockId] ?: listOf()
	fun getMostModernReadableOverlay(skyblockId: SkyblockId, version: Int) = getOverlayFiles(skyblockId)
		.filter { it.version <= version }
		.maxByOrNull { it.version }
}

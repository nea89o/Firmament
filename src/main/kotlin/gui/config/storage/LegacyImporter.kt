package moe.nea.firmament.gui.config.storage

import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.writeText
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.configFolder
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.configVersionFile
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.storageFolder

object LegacyImporter {
	val legacyConfigVersion = 995
	val backupPath = configFolder.resolveSibling("firmament-legacy-config-${System.currentTimeMillis()}")

	fun copyIf(from: Path, to: Path) {
		if (from.exists()) {
			to.createParentDirectories()
			from.copyTo(to)
		}
	}

	val legacyStorage = listOf(
		"inventory-buttons",
		"macros",
	)

	fun importFromLegacy() {
		Firmament.logger.info("Importing legacy config")
		if (!configFolder.exists()) return
		configFolder.moveTo(backupPath)
		configFolder.createDirectories()

		legacyStorage.forEach {
			copyIf(
				backupPath.resolve("$it.json"),
				storageFolder.resolve("$it.json")
			)
		}

		backupPath.listDirectoryEntries("*.json")
			.filter { it.nameWithoutExtension !in legacyStorage }
			.forEach { path ->
				val name = path.name
				path.copyTo(configFolder.resolve(name))
			}

		backupPath.resolve("profiles")
			.takeIf { it.exists() }
			?.forEachDirectoryEntry { category ->
				category.forEachDirectoryEntry { profile ->
					copyIf(
						profile,
						FirmamentConfigLoader.profilePath
							.resolve(profile.nameWithoutExtension)
							.resolve(category.name + ".json")
					)
				}
			}

		configVersionFile.writeText("$legacyConfigVersion LEGACY")
	}
}

package moe.nea.firmament.util.data

import kotlinx.serialization.KSerializer
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText
import moe.nea.firmament.Firmament

abstract class MultiFileDataHolder<T>(
	val dataSerializer: KSerializer<T>,
	val configName: String
) { // TODO: abstract this + ProfileSpecificDataHolder
	val configDirectory = Firmament.CONFIG_DIR.resolve(configName)
	private var allData = readValues()
	protected fun readValues(): MutableMap<String, T> {
		if (!configDirectory.exists()) {
			configDirectory.createDirectories()
		}
		val profileFiles = configDirectory.listDirectoryEntries()
		return profileFiles
			.filter { it.extension == "json" }
			.mapNotNull {
				try {
					it.nameWithoutExtension to Firmament.json.decodeFromString(dataSerializer, it.readText())
				} catch (e: Exception) { /* Expecting IOException and SerializationException, but Kotlin doesn't allow multi catches*/
					IDataHolder.badLoads.add(configName)
					Firmament.logger.error(
						"Exception during loading of multi file data holder $it ($configName). This will reset that profiles config.",
						e
					)
					null
				}
			}.toMap().toMutableMap()
	}

	fun save() {
		if (!configDirectory.exists()) {
			configDirectory.createDirectories()
		}
		val c = allData
		configDirectory.listDirectoryEntries().forEach {
			if (it.nameWithoutExtension !in c.mapKeys { it.toString() }) {
				it.deleteExisting()
			}
		}
		c.forEach { (name, value) ->
			val f = configDirectory.resolve("$name.json")
			f.writeText(Firmament.json.encodeToString(dataSerializer, value))
		}
	}

	fun list(): Map<String, T> = allData
	val validPathRegex = "[a-zA-Z0-9_][a-zA-Z0-9\\-_.]*".toPattern()
	fun insert(name: String, value: T) {
		require(validPathRegex.matcher(name).matches()) { "Not a valid name: $name" }
		allData[name] = value
	}
}

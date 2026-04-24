package moe.nea.firmament.gui.config.storage

import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.debug.DebugLogger
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.SBData.NULL_UUID
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.data.IConfigProvider
import moe.nea.firmament.util.data.IDataHolder
import moe.nea.firmament.util.data.ProfileKeyedConfig
import moe.nea.firmament.util.json.intoGson
import moe.nea.firmament.util.json.intoKotlinJson

object FirmamentConfigLoader {
	val currentConfigVersion = 1000
	val configFolder = Path("config/firmament")
		.toAbsolutePath()
	val storageFolder = configFolder.resolve("storage")
	val profilePath = configFolder.resolve("profiles")
	val tagLines = listOf(
		"<- your config version here",
		"I'm a teapot",
		"mail.example.com ESMTP",
		"Apples"
	)
	val configVersionFile = configFolder.resolve("config.version")

	fun loadConfig() {
		if (configFolder.exists()) {
			if (!configVersionFile.exists()) {
				LegacyImporter.importFromLegacy()
			}
			updateConfigs()
		}

		ConfigLoadContext("load-${System.currentTimeMillis()}").use { loadContext ->
			val configData = FirstLevelSplitJsonFolder(loadContext, configFolder).load()
			loadConfigFromData(configData, Unit, ConfigStorageClass.CONFIG)
			val storageData = FirstLevelSplitJsonFolder(loadContext, storageFolder).load()
			loadConfigFromData(storageData, Unit, ConfigStorageClass.STORAGE)
			var profileData =
				profilePath.takeIf { it.exists() }
					?.listDirectoryEntries()
					?.filter { it.isDirectory() }
					?.mapNotNull {
						val uuid = runCatching { UUID.fromString(it.name) }.getOrNull() ?: return@mapNotNull null
						uuid to FirstLevelSplitJsonFolder(loadContext, it).load()
					}
					?.toMap()
			if (profileData.isNullOrEmpty())
				profileData = mapOf(NULL_UUID to JsonObject(mapOf()))
			profileData.forEach { (key, value) ->
				loadConfigFromData(value, key, ConfigStorageClass.PROFILE)
			}
		}
	}

	fun <T> loadConfigFromData(
		configData: JsonObject,
		key: T?,
		storageClass: ConfigStorageClass
	) {
		for (holder in allConfigs) {
			if (holder.storageClass == storageClass) {
				val h = (holder as IDataHolder<T>)
				if (key == null) {
					h.explicitDefaultLoad()
				} else {
					h.loadFrom(key, configData)
				}
			}
		}
	}

	fun <T> collectConfigFromData(
		key: T,
		storageClass: ConfigStorageClass,
	): JsonObject {
		var json = JsonObject(mapOf())
		for (holder in allConfigs) {
			if (holder.storageClass == storageClass) {
				json = mergeJson(json, (holder as IDataHolder<T>).saveTo(key))
			}
		}
		return json
	}

	fun <T> saveStorage(
		storageClass: ConfigStorageClass,
		key: T,
		firstLevelSplitJsonFolder: FirstLevelSplitJsonFolder,
	) {
		firstLevelSplitJsonFolder.save(
			collectConfigFromData(key, storageClass)
		)
	}

	fun collectAllProfileIds(): Set<UUID> {
		return allConfigs
			.filter { it.storageClass == ConfigStorageClass.PROFILE }
			.flatMapTo(mutableSetOf()) {
				(it as ProfileKeyedConfig<*>).keys()
			}
	}

	fun saveAll() {
		ConfigLoadContext("save-${System.currentTimeMillis()}").use { context ->
			saveStorage(
				ConfigStorageClass.CONFIG,
				Unit,
				FirstLevelSplitJsonFolder(context, configFolder)
			)
			saveStorage(
				ConfigStorageClass.STORAGE,
				Unit,
				FirstLevelSplitJsonFolder(context, storageFolder)
			)
			collectAllProfileIds().forEach { profileId ->
				saveStorage(
					ConfigStorageClass.PROFILE,
					profileId,
					FirstLevelSplitJsonFolder(context, profilePath.resolve(profileId.toString()))
				)
			}
			writeConfigVersion()
		}
	}

	fun mergeJson(a: JsonObject, b: JsonObject): JsonObject {
		fun mergeInner(a: JsonElement?, b: JsonElement?): JsonElement {
			if (a == null)
				return b!!
			if (b == null)
				return a
			a as JsonObject
			b as JsonObject
			return buildJsonObject {
				(a.keys + b.keys)
					.forEach {
						put(it, mergeInner(a[it], b[it]))
					}
			}
		}
		return mergeInner(a, b) as JsonObject
	}

	val allConfigs: List<IDataHolder<*>> = IConfigProvider.providers.allValidInstances.flatMap { it.configs }

	fun updateConfigs() {
		val configVersionText = configVersionFile.readText()
		val startVersion = ErrorUtil.catch("Could not parse old config version from '$configVersionText'") {
			configVersionText
				.substringBefore(' ')
				.trim()
				.toInt()
		}.or { return }
		ConfigLoadContext("update-from-$startVersion-to-$currentConfigVersion-${System.currentTimeMillis()}")
			.use { loadContext ->
				updateOneConfig(
					loadContext,
					startVersion,
					ConfigStorageClass.CONFIG,
					FirstLevelSplitJsonFolder(loadContext, configFolder)
				)
				updateOneConfig(
					loadContext,
					startVersion,
					ConfigStorageClass.STORAGE,
					FirstLevelSplitJsonFolder(loadContext, storageFolder)
				)
				profilePath.forEachDirectoryEntry {
					updateOneConfig(
						loadContext,
						startVersion,
						ConfigStorageClass.PROFILE,
						FirstLevelSplitJsonFolder(loadContext, it)
					)
				}
				writeConfigVersion()
			}
	}

	fun writeConfigVersion() {
		configVersionFile.writeText("$currentConfigVersion ${tagLines.random()}")
	}

	private fun updateOneConfig(
		loadContext: ConfigLoadContext,
		startVersion: Int,
		storageClass: ConfigStorageClass,
		firstLevelSplitJsonFolder: FirstLevelSplitJsonFolder
	) {
		if (startVersion == currentConfigVersion) {
			loadContext.logDebug("Skipping upgrade to ")
			return
		}
		loadContext.logInfo("Starting upgrade from at ${firstLevelSplitJsonFolder.folder} ($storageClass) to $startVersion")
		var data = firstLevelSplitJsonFolder.load()
		for (nextVersion in (startVersion + 1)..currentConfigVersion) {
			data = updateOneConfigOnce(nextVersion, storageClass, data)
		}
		firstLevelSplitJsonFolder.save(data)
	}

	private fun updateOneConfigOnce(
		nextVersion: Int,
		storageClass: ConfigStorageClass,
		data: JsonObject
	): JsonObject {
		return ConfigFixEvent.publish(ConfigFixEvent(storageClass, nextVersion, data.intoGson().asJsonObject))
			.data.intoKotlinJson().jsonObject
	}

	@Subscribe
	fun onTick(event: TickEvent) {
		val config = configPromise ?: return
		val passedTime = saveDebounceStart.passedTime()
		if (passedTime < 1.seconds)
			return
		if (!config.isDone && passedTime < 3.seconds)
			return
		debugLogger.log("Performing config save")
		configPromise = null
		saveAll()
	}

	val debugLogger = DebugLogger("config")

	var configPromise: CompletableFuture<Void?>? = null
	var saveDebounceStart: TimeMark = TimeMark.farPast()
	fun markDirty(
		holder: IDataHolder<*>,
		timeoutPromise: CompletableFuture<Void?>? = null
	) {
		debugLogger.log("Config marked dirty")
		this.saveDebounceStart = TimeMark.now()
		this.configPromise = timeoutPromise ?: CompletableFuture.completedFuture(null)
	}

}

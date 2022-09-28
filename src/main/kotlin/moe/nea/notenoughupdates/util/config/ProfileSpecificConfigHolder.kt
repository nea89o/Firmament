package moe.nea.notenoughupdates.util.config

import java.nio.file.Path
import kotlinx.serialization.KSerializer
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.util.SBData

abstract class ProfileSpecificConfigHolder<S>(
    private val configSerializer: KSerializer<S>,
    val configName: String,
    private val configDefault: () -> S
) : IConfigHolder<S?> {

    var allConfigs: MutableMap<String, S>

    override val config: S?
        get() = SBData.profileCuteName?.let {
            allConfigs.computeIfAbsent(it) { configDefault() }
        }

    init {
        allConfigs = readValues()
        readValues()
    }

    private val configDirectory: Path get() = NotEnoughUpdates.CONFIG_DIR.resolve("profiles")

    private fun readValues(): MutableMap<String, S> {
        if (!configDirectory.exists()) {
            configDirectory.createDirectories()
        }
        val profileFiles = configDirectory.listDirectoryEntries()
        return profileFiles
            .filter { it.extension == "json" }
            .mapNotNull {
                try {
                    it.nameWithoutExtension to NotEnoughUpdates.json.decodeFromString(configSerializer, it.readText())
                } catch (e: Exception) { /* Expecting IOException and SerializationException, but Kotlin doesn't allow multi catches*/
                    IConfigHolder.badLoads.add(configName)
                    NotEnoughUpdates.logger.error(
                        "Exception during loading of profile specific config file $it ($configName). This will reset that profiles config.",
                        e
                    )
                    null
                }
            }.toMap().toMutableMap()
    }

    override fun save() {
        if (!configDirectory.exists()) {
            configDirectory.createDirectories()
        }
        val c = allConfigs
        configDirectory.listDirectoryEntries().forEach {
            if (it.nameWithoutExtension !in c) {
                it.deleteExisting()
            }
        }
        c.forEach { (name, value) ->
            val f = configDirectory.resolve("$name.json")
            f.writeText(NotEnoughUpdates.json.encodeToString(configSerializer, value))
        }
    }

    override fun markDirty() {
        IConfigHolder.markDirty(this::class)
    }

    override fun load() {
        allConfigs = readValues()
    }

}

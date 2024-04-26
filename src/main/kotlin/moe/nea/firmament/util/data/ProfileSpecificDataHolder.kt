/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.data

import java.nio.file.Path
import java.util.UUID
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
import moe.nea.firmament.util.SBData

abstract class ProfileSpecificDataHolder<S>(
    private val dataSerializer: KSerializer<S>,
    val configName: String,
    private val configDefault: () -> S
) : IDataHolder<S?> {

    var allConfigs: MutableMap<UUID, S>

    override val data: S?
        get() = SBData.profileId?.let {
            allConfigs.computeIfAbsent(it) { configDefault() }
        }

    init {
        allConfigs = readValues()
        IDataHolder.putDataHolder(this::class, this)
    }

    private val configDirectory: Path get() = Firmament.CONFIG_DIR.resolve("profiles").resolve(configName)

    private fun readValues(): MutableMap<UUID, S> {
        if (!configDirectory.exists()) {
            configDirectory.createDirectories()
        }
        val profileFiles = configDirectory.listDirectoryEntries()
        return profileFiles
            .filter { it.extension == "json" }
            .mapNotNull {
                try {
                    UUID.fromString(it.nameWithoutExtension) to Firmament.json.decodeFromString(dataSerializer, it.readText())
                } catch (e: Exception) { /* Expecting IOException and SerializationException, but Kotlin doesn't allow multi catches*/
                    IDataHolder.badLoads.add(configName)
                    Firmament.logger.error(
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
            if (it.nameWithoutExtension !in c.mapKeys { it.toString() }) {
                it.deleteExisting()
            }
        }
        c.forEach { (name, value) ->
            val f = configDirectory.resolve("$name.json")
            f.writeText(Firmament.json.encodeToString(dataSerializer, value))
        }
    }

    override fun markDirty() {
        IDataHolder.markDirty(this::class)
    }

    override fun load() {
        allConfigs = readValues()
    }

}

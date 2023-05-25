/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.util.data

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
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.SBData

abstract class ProfileSpecificDataHolder<S>(
    private val dataSerializer: KSerializer<S>,
    val configName: String,
    private val configDefault: () -> S
) : IDataHolder<S?> {

    var allConfigs: MutableMap<String, S>

    override val data: S?
        get() = SBData.profileCuteName?.let {
            allConfigs.computeIfAbsent(it) { configDefault() }
        }

    init {
        allConfigs = readValues()
        readValues()
        IDataHolder.putDataHolder(this::class, this)
    }

    private val configDirectory: Path get() = Firmament.CONFIG_DIR.resolve("profiles").resolve(configName)

    private fun readValues(): MutableMap<String, S> {
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

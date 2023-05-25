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
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import moe.nea.firmament.Firmament

abstract class DataHolder<T>(
    val serializer: KSerializer<T>,
    val name: String,
    val default: () -> T
) : IDataHolder<T> {


    final override var data: T
        private set

    init {
        data = readValueOrDefault()
        IDataHolder.putDataHolder(this::class, this)
    }

    private val file: Path get() = Firmament.CONFIG_DIR.resolve("$name.json")

    protected fun readValueOrDefault(): T {
        if (file.exists())
            try {
                return Firmament.json.decodeFromString(
                    serializer,
                    file.readText()
                )
            } catch (e: Exception) {/* Expecting IOException and SerializationException, but Kotlin doesn't allow multi catches*/
                IDataHolder.badLoads.add(name)
                Firmament.logger.error(
                    "Exception during loading of config file $name. This will reset this config.",
                    e
                )
            }
        return default()
    }

    private fun writeValue(t: T) {
        file.writeText(Firmament.json.encodeToString(serializer, t))
    }

    override fun save() {
        writeValue(data)
    }

    override fun load() {
        data = readValueOrDefault()
    }

    override fun markDirty() {
        IDataHolder.markDirty(this::class)
    }

}

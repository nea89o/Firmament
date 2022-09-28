package moe.nea.notenoughupdates.util.config

import java.nio.file.Path
import kotlinx.serialization.KSerializer
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import moe.nea.notenoughupdates.NotEnoughUpdates

abstract class ConfigHolder<T>(
    val serializer: KSerializer<T>,
    val name: String,
    val default: () -> T
) : IConfigHolder<T> {


    final override var config: T
        private set

    init {
        config = readValueOrDefault()
        IConfigHolder.putConfig(this::class, this)
    }

    private val file: Path get() = NotEnoughUpdates.CONFIG_DIR.resolve("$name.json")

    protected fun readValueOrDefault(): T {
        if (file.exists())
            try {
                return NotEnoughUpdates.json.decodeFromString(
                    serializer,
                    file.readText()
                )
            } catch (e: Exception) {/* Expecting IOException and SerializationException, but Kotlin doesn't allow multi catches*/
                IConfigHolder.badLoads.add(name)
                NotEnoughUpdates.logger.error(
                    "Exception during loading of config file $name. This will reset this config.",
                    e
                )
            }
        return default()
    }

    private fun writeValue(t: T) {
        file.writeText(NotEnoughUpdates.json.encodeToString(serializer, t))
    }

    override fun save() {
        writeValue(config)
    }

    override fun load() {
        config = readValueOrDefault()
    }

    override fun markDirty() {
        IConfigHolder.markDirty(this::class)
    }

}

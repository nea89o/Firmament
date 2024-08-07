

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

package moe.nea.notenoughupdates.util

import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import net.minecraft.client.MinecraftClient
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.events.ScreenOpenEvent

abstract class ConfigHolder<T>(
    val serializer: KSerializer<T>,
    val name: String,
    val default: () -> T
) {

    var config: T
        private set

    init {
        config = readValueOrDefault()
        putConfig(this::class, this)
    }

    val file: Path get() = NotEnoughUpdates.CONFIG_DIR.resolve("$name.json")

    protected fun readValueOrDefault(): T {
        if (file.exists())
            try {
                return NotEnoughUpdates.json.decodeFromString(
                    serializer,
                    file.readText()
                )
            } catch (e: IOException) {
                badLoads.add(name)
                NotEnoughUpdates.logger.error(
                    "IO exception during loading of config file $name. This will reset this config.",
                    e
                )
            } catch (e: SerializationException) {
                badLoads.add(name)
                NotEnoughUpdates.logger.error(
                    "Serialization exception during loading of config file $name. This will reset this config.",
                    e
                )
            }
        return default()
    }

    private fun writeValue(t: T) {
        file.writeText(NotEnoughUpdates.json.encodeToString(serializer, t))
    }

    fun save() {
        writeValue(config)
    }

    fun load() {
        config = readValueOrDefault()
    }

    fun markDirty() {
        Companion.markDirty(this::class)
    }

    companion object {
        private var badLoads: MutableList<String> = CopyOnWriteArrayList()
        private val allConfigs: MutableMap<KClass<out ConfigHolder<*>>, ConfigHolder<*>> = mutableMapOf()
        private val dirty: MutableSet<KClass<out ConfigHolder<*>>> = mutableSetOf()

        private fun <T : ConfigHolder<K>, K> putConfig(kClass: KClass<T>, inst: ConfigHolder<K>) {
            allConfigs[kClass] = inst
        }

        fun <T : ConfigHolder<K>, K> markDirty(kClass: KClass<T>) {
            if (kClass !in allConfigs) {
                NotEnoughUpdates.logger.error("Tried to markDirty '${kClass.qualifiedName}', which isn't registered as 'ConfigHolder'")
                return
            }
            dirty.add(kClass)
        }

        private fun performSaves() {
            val toSave = dirty.toList().also {
                dirty.clear()
            }
            for (it in toSave) {
                val obj = allConfigs[it]
                if (obj == null) {
                    NotEnoughUpdates.logger.error("Tried to save '${it}', which isn't registered as 'ConfigHolder'")
                    continue
                }
                obj.save()
            }
        }

        private fun warnForResetConfigs(player: CommandOutput) {
            if (badLoads.isNotEmpty()) {
                player.sendMessage(
                    Text.literal(
                        "The following configs have been reset: ${badLoads.joinToString(", ")}. " +
                            "This can be intentional, but probably isn't."
                    )
                )
                badLoads.clear()
            }
        }

        fun registerEvents() {
            ScreenOpenEvent.subscribe { event ->
                performSaves()
                val p = MinecraftClient.getInstance().player
                if (p != null) {
                    warnForResetConfigs(p)
                }
            }
            ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping {
                performSaves()
            })
        }

    }
}

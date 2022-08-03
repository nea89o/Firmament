package moe.nea.notenoughupdates.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.events.NEUScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSource
import net.minecraft.network.chat.Component
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass

abstract class ConfigHolder<T>(val serializer: KSerializer<T>,
                               val name: String,
                               val default: () -> T) {

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
                NotEnoughUpdates.logger.error("IO exception during loading of config file $name. This will reset this config.", e)
            } catch (e: SerializationException) {
                badLoads.add(name)
                NotEnoughUpdates.logger.error("Serialization exception during loading of config file $name. This will reset this config.", e)
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

        private fun warnForResetConfigs(player: CommandSource) {
            if (badLoads.isNotEmpty()) {
                player.sendSystemMessage(Component.literal("The following configs have been reset: ${badLoads.joinToString(", ")}. " +
                        "This can be intentional, but probably isn't."))
                badLoads.clear()
            }
        }

        fun registerEvents() {
            NEUScreenEvents.SCREEN_OPEN.register(NEUScreenEvents.OnScreenOpen { old, new ->
                performSaves()
                val p = Minecraft.getInstance().player
                if (p != null) {
                    warnForResetConfigs(p)
                }
                false
            })
        }


    }
}
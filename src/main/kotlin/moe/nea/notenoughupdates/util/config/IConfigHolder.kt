package moe.nea.notenoughupdates.util.config

import java.util.concurrent.CopyOnWriteArrayList
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import kotlin.reflect.KClass
import net.minecraft.client.MinecraftClient
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.events.ScreenOpenEvent

interface IConfigHolder<T> {
    companion object {
        internal var badLoads: MutableList<String> = CopyOnWriteArrayList()
        private val allConfigs: MutableMap<KClass<out IConfigHolder<*>>, IConfigHolder<*>> = mutableMapOf()
        private val dirty: MutableSet<KClass<out IConfigHolder<*>>> = mutableSetOf()

        internal fun <T : IConfigHolder<K>, K> putConfig(kClass: KClass<T>, inst: IConfigHolder<K>) {
            allConfigs[kClass] = inst
        }

        fun <T : IConfigHolder<K>, K> markDirty(kClass: KClass<T>) {
            if (kClass !in allConfigs) {
                NotEnoughUpdates.logger.error("Tried to markDirty '${kClass.qualifiedName}', which isn't registered as 'IConfigHolder'")
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

    val config: T
    fun save()
    fun markDirty()
    fun load()
}

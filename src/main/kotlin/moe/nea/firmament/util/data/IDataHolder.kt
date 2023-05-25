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

import java.util.concurrent.CopyOnWriteArrayList
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import kotlin.reflect.KClass
import net.minecraft.client.MinecraftClient
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.ScreenOpenEvent

interface IDataHolder<T> {
    companion object {
        internal var badLoads: MutableList<String> = CopyOnWriteArrayList()
        private val allConfigs: MutableMap<KClass<out IDataHolder<*>>, IDataHolder<*>> = mutableMapOf()
        private val dirty: MutableSet<KClass<out IDataHolder<*>>> = mutableSetOf()

        internal fun <T : IDataHolder<K>, K> putDataHolder(kClass: KClass<T>, inst: IDataHolder<K>) {
            allConfigs[kClass] = inst
        }

        fun <T : IDataHolder<K>, K> markDirty(kClass: KClass<T>) {
            if (kClass !in allConfigs) {
                Firmament.logger.error("Tried to markDirty '${kClass.qualifiedName}', which isn't registered as 'IConfigHolder'")
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
                    Firmament.logger.error("Tried to save '${it}', which isn't registered as 'ConfigHolder'")
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

    val data: T
    fun save()
    fun markDirty()
    fun load()
}

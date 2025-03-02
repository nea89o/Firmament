package moe.nea.firmament.util.data

import java.util.concurrent.CopyOnWriteArrayList
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import kotlin.reflect.KClass
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.util.MC

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

		private fun warnForResetConfigs() {
			if (badLoads.isNotEmpty()) {
				MC.sendChat(
					Text.literal(
						"The following configs have been reset: ${badLoads.joinToString(", ")}. " +
							"This can be intentional, but probably isn't."
					)
				)
				badLoads.clear()
			}
		}

		fun registerEvents() {
			ScreenChangeEvent.subscribe("IDataHolder:saveOnScreenChange") { event ->
				performSaves()
				warnForResetConfigs()
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

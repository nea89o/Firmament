package moe.nea.firmament.util.compatloader

import java.util.ServiceLoader
import net.fabricmc.loader.api.FabricLoader
import kotlin.reflect.KClass
import kotlin.streams.asSequence
import moe.nea.firmament.Firmament

open class CompatLoader<T : Any>(val kClass: Class<T>) {
	constructor(kClass: KClass<T>) : this(kClass.java)

	val loader: ServiceLoader<T> = ServiceLoader.load(kClass)
	val allValidInstances by lazy {
		loader.reload()
		loader.stream()
			.asSequence()
			.filter { provider ->
				runCatching {
					shouldLoad(provider.type())
				}.getOrElse {
					Firmament.logger.error("Could not determine whether to load a ${kClass.name} subclass", it)
					false
				}
			}
			.mapNotNull { provider ->
				runCatching {
					provider.get()
				}.getOrElse {
					Firmament.logger.error(
						"Could not load desired instance ${provider.type().name} for ${kClass.name}",
						it)
					null
				}
			}
			.toList()
	}
	val singleInstance by lazy { allValidInstances.singleOrNull() }

	open fun shouldLoad(type: Class<out T>): Boolean {
		return checkRequiredModsPresent(type)
	}

	fun checkRequiredModsPresent(type: Class<*>): Boolean {
		val requiredMods = type.getAnnotationsByType(RequireMod::class.java)
		return requiredMods.all { FabricLoader.getInstance().isModLoaded(it.modId) }
	}

	@Repeatable
	annotation class RequireMod(val modId: String)
}

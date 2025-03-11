package moe.nea.firmament.util.compatloader

import java.util.ServiceLoader
import net.fabricmc.loader.api.FabricLoader
import kotlin.reflect.KClass
import kotlin.streams.asSequence
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.ErrorUtil

open class CompatLoader<T : Any>(val kClass: Class<T>) {
	constructor(kClass: KClass<T>) : this(kClass.java)

	val loader: ServiceLoader<T> = ServiceLoader.load(kClass)
	val allValidInstances by lazy {
		val resources = kClass.classLoader.getResources("META-INF/services/${kClass.name}")
		val classes = resources
			.asSequence()
			.map { ErrorUtil.catch("Could not read service loader resource at $it") { it.readText() }.or { "" } }
			.flatMap { it.lineSequence() }
			.map { it.substringBefore('#').trim() }
			.filter { it.isNotBlank() }
			.mapNotNull {
				ErrorUtil.catch("Could not load class named $it for $kClass") {
					Class.forName(it,
					              false,
					              kClass.classLoader).asSubclass(kClass)
				}.or { null }
			}
			.toList()

		classes.asSequence()
			.filter { clazz ->
				runCatching {
					shouldLoad(clazz)
				}.getOrElse {
					Firmament.logger.error("Could not determine whether to load a ${kClass.name} subclass", it)
					false
				}
			}
			.mapNotNull { clazz ->
				runCatching {
					clazz.kotlin.objectInstance ?: clazz.getConstructor().newInstance()
				}.getOrElse {
					Firmament.logger.error(
						"Could not load desired instance ${clazz.name} for ${kClass.name}",
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

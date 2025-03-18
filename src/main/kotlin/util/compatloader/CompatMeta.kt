package moe.nea.firmament.util.compatloader

import java.util.ServiceLoader
import moe.nea.firmament.events.subscription.SubscriptionList
import moe.nea.firmament.init.AutoDiscoveryPlugin
import moe.nea.firmament.util.ErrorUtil

/**
 * Declares the compat meta interface for the current source set.
 * This is used by [CompatLoader], [SubscriptionList], and [AutoDiscoveryPlugin]. Annotate a [ICompatMeta] object with
 * this.
 */
annotation class CompatMeta

interface ICompatMetaGen {
	fun owns(className: String): Boolean
	val meta: ICompatMeta
}

interface ICompatMeta {
	fun shouldLoad(): Boolean

	companion object {
		val allMetas = ServiceLoader
			.load(ICompatMetaGen::class.java)
			.toList()

		fun shouldLoad(className: String): Boolean {
			// TODO: replace this with a more performant package lookup
			val meta = if (ErrorUtil.aggressiveErrors) {
				val fittingMetas = allMetas.filter { it.owns(className) }
				require(fittingMetas.size == 1) { "Orphaned or duplicate owned class $className (${fittingMetas.map { it.meta }}). Consider adding a @CompatMeta object." }
				fittingMetas.single()
			} else {
				allMetas.firstOrNull { it.owns(className) }
			}
			return meta?.meta?.shouldLoad() ?: true
		}
	}
}

object CompatHelper {
	fun isOwnedByPackage(className: String, vararg packages: String): Boolean {
		// TODO: create package lookup structure once
		val packageName = className.substringBeforeLast('.')
		return packageName in packages
	}
}

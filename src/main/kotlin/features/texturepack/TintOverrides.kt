package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.assertNotNullOr

data class TintOverrides(
	val layerMap: Map<Int, TintOverride> = mapOf()
) {
	val hasOverrides by lazy { layerMap.values.any { it !is Reset } }

	companion object {
		val EMPTY = TintOverrides()
		private val threadLocal = object : ThreadLocal<TintOverrides>() {}
		fun enter(overrides: TintOverrides?) {
			ErrorUtil.softCheck("Double entered tintOverrides") {
				threadLocal.get() == null
			}
			threadLocal.set(overrides ?: EMPTY)
		}

		fun exit(overrides: TintOverrides?) {
			ErrorUtil.softCheck("Exited with non matching enter tintOverrides") {
				threadLocal.get() == (overrides ?: EMPTY)
			}
			threadLocal.remove()
		}

		fun getCurrentOverrides() =
			assertNotNullOr(threadLocal.get(), "Got current tintOverrides without entering") { EMPTY }

		fun parse(jsonObject: JsonObject): TintOverrides {
			val map = mutableMapOf<Int, TintOverride>()
			for ((key, value) in jsonObject.entrySet()) {
				val layerIndex =
					ErrorUtil.notNullOr(key.toIntOrNull(),
					                    "Unknown layer index $value. Should be integer") { continue }
				if (value.isJsonNull) {
					map[layerIndex] = Reset
					continue
				}
				val override = (value as? JsonPrimitive)
					?.takeIf(JsonPrimitive::isNumber)
					?.asInt
					?.let(::Fixed)
				if (override == null) {
					ErrorUtil.softError("Invalid tint override for a layer: $value")
					continue
				}
				map[layerIndex] = override
			}
			return TintOverrides(map)
		}
	}

	fun mergeWithParent(parent: TintOverrides): TintOverrides {
		val mergedMap = parent.layerMap.toMutableMap()
		mergedMap.putAll(this.layerMap)
		return TintOverrides(mergedMap)
	}

	fun hasOverrides(): Boolean = hasOverrides
	fun getOverride(tintIndex: Int): Int? {
		return when (val tint = layerMap[tintIndex]) {
			is Reset -> null
			is Fixed -> tint.color
			null -> null
		}
	}

	sealed interface TintOverride
	data object Reset : TintOverride
	data class Fixed(val color: Int) : TintOverride
}

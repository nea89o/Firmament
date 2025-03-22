package moe.nea.firmament.features.world

import kotlinx.serialization.Serializable
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.ErrorUtil

object ColeWeightCompat {
	@Serializable
	data class ColeWeightWaypoint(
		val x: Int,
		val y: Int,
		val z: Int,
		val r: Int = 0,
		val g: Int = 0,
		val b: Int = 0,
	)

	fun intoFirm(waypoints: List<ColeWeightWaypoint>): FirmWaypoints {
		val w = waypoints.map {
			FirmWaypoints.Waypoint(it.x, it.y, it.z)
		}
		return FirmWaypoints(
			"Imported Waypoints",
			"imported",
			null,
			w.toMutableList(),
			false
		)
	}

	fun tryParse(string: String): Result<List<ColeWeightWaypoint>> {
		return runCatching {
			Firmament.tightJson.decodeFromString<List<ColeWeightWaypoint>>(string)
		}
	}
}

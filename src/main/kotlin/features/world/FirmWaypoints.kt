package moe.nea.firmament.features.world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.util.math.BlockPos

@Serializable
data class FirmWaypoints(
	var label: String,
	var id: String,
	/**
	 * A hint to indicate where to stand while loading the waypoints.
	 */
	var isRelativeTo: String?,
	var waypoints: MutableList<Waypoint>,
	var isOrdered: Boolean,
	// TODO: val resetOnSwap: Boolean,
) {

	fun deepCopy() = copy(waypoints = waypoints.toMutableList())
	@Transient
	var lastRelativeImport: BlockPos? = null

	val size get() = waypoints.size
	@Serializable
	data class Waypoint(
		val x: Int,
		val y: Int,
		val z: Int,
	) {
		val blockPos get() = BlockPos(x, y, z)

		companion object {
			fun from(blockPos: BlockPos) = Waypoint(blockPos.x, blockPos.y, blockPos.z)
		}
	}
}

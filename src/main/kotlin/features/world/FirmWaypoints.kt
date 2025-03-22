package moe.nea.firmament.features.world

import net.minecraft.util.math.BlockPos

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
	val size get() = waypoints.size
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

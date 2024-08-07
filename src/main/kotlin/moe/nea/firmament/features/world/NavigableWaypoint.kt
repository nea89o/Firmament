package moe.nea.firmament.features.world

import io.github.moulberry.repo.data.NEUItem
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.util.SkyBlockIsland

abstract class NavigableWaypoint {
    abstract val name: String
    abstract val position: BlockPos
    abstract val island: SkyBlockIsland

    data class NPCWaypoint(
        val item: NEUItem,
    ) : NavigableWaypoint() {
        override val name: String
            get() = item.displayName
        override val position: BlockPos
            get() = BlockPos(item.x, item.y, item.z)
        override val island: SkyBlockIsland
            get() = SkyBlockIsland.forMode(item.island)
    }
}

package moe.nea.firmament.features.world

import io.github.moulberry.repo.constants.Islands
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Position
import net.minecraft.util.math.Vec3i
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.WarpUtil
import moe.nea.firmament.util.render.RenderInWorldContext

object NavigationHelper {
    var targetWaypoint: NavigableWaypoint? = null
        set(value) {
            field = value
            recalculateRoute()
        }

    var nextTeleporter: Islands.Teleporter? = null
        private set

    val Islands.Teleporter.toIsland get() = SkyBlockIsland.forMode(this.getTo())
    val Islands.Teleporter.fromIsland get() = SkyBlockIsland.forMode(this.getFrom())
    val Islands.Teleporter.blockPos get() = BlockPos(x.toInt(), y.toInt(), z.toInt())

    @Subscribe
    fun onWorldSwitch(event: SkyblockServerUpdateEvent) {
        recalculateRoute()
    }

    fun recalculateRoute() {
        val tp = targetWaypoint
        val currentIsland = SBData.skyblockLocation
        if (tp == null || currentIsland == null) {
            nextTeleporter = null
            return
        }
        val route = findRoute(currentIsland, tp.island, mutableSetOf())
        nextTeleporter = route?.get(0)
    }

    private fun findRoute(
        fromIsland: SkyBlockIsland,
        targetIsland: SkyBlockIsland,
        visitedIslands: MutableSet<SkyBlockIsland>
    ): MutableList<Islands.Teleporter>? {
        var shortestChain: MutableList<Islands.Teleporter>? = null
        for (it in RepoManager.neuRepo.constants.islands.teleporters) {
            if (it.toIsland in visitedIslands) continue
            if (it.fromIsland != fromIsland) continue
            if (it.toIsland == targetIsland) return mutableListOf(it)
            visitedIslands.add(fromIsland)
            val nextRoute = findRoute(it.toIsland, targetIsland, visitedIslands) ?: continue
            nextRoute.add(0, it)
            if (shortestChain == null || shortestChain.size > nextRoute.size) {
                shortestChain = nextRoute
            }
            visitedIslands.remove(fromIsland)
        }
        return shortestChain
    }


    @Subscribe
    fun onMovement(event: TickEvent) { // TODO: add a movement tick event maybe?
        val tp = targetWaypoint ?: return
        val p = MC.player ?: return
        if (p.squaredDistanceTo(tp.position.toCenterPos()) < 5 * 5) {
            targetWaypoint = null
        }
    }

    @Subscribe
    fun drawWaypoint(event: WorldRenderLastEvent) {
        val tp = targetWaypoint ?: return
        val nt = nextTeleporter
        RenderInWorldContext.renderInWorld(event) {
            if (nt != null) {
                waypoint(nt.blockPos,
                         Text.literal("Teleporter to " + nt.toIsland.userFriendlyName),
                         Text.literal("(towards " + tp.name + "§f)"))
            } else if (tp.island == SBData.skyblockLocation) {
                waypoint(tp.position,
                         Text.literal(tp.name))
            }
        }
    }

    fun tryWarpNear() {
        val tp = targetWaypoint
        if (tp == null) {
            MC.sendChat(Text.literal("Could not find a waypoint to warp you to. Select one first."))
            return
        }
        WarpUtil.teleportToNearestWarp(tp.island, tp.position.asPositionView())
    }

}

fun Vec3i.asPositionView(): Position {
    return object : Position {
        override fun getX(): Double {
            return this@asPositionView.x.toDouble()
        }

        override fun getY(): Double {
            return this@asPositionView.y.toDouble()
        }

        override fun getZ(): Double {
            return this@asPositionView.z.toDouble()
        }
    }
}

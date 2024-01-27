/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.world

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.render.RenderInWorldContext

object Waypoints : FirmamentFeature {
    override val identifier: String
        get() = "waypoints"

    object TConfig : ManagedConfig(identifier) {
        val tempWaypointDuration by duration("temp-waypoint-duration", 0.seconds, 1.hours) { 30.seconds }
    }

    data class TemporaryWaypoint(
        val pos: BlockPos,
        val postedAt: TimeMark,
    )

    override val config get() = TConfig

    val temporaryWaypointList = mutableMapOf<String, TemporaryWaypoint>()
    val temporaryWaypointMatcher = "x: (-?[0-9]+),? y: (-?[0-9]+),? z: (-?[0-9]+)".toPattern()
    override fun onLoad() {
        WorldRenderLastEvent.subscribe {
            temporaryWaypointList.entries.removeIf { it.value.postedAt.passedTime() > TConfig.tempWaypointDuration }
            if (temporaryWaypointList.isNotEmpty())
                RenderInWorldContext.renderInWorld(it) {
                    color(1f, 1f, 0f, 1f)
                    temporaryWaypointList.forEach { (player, waypoint) ->
                        block(waypoint.pos)
                    }
                    color(1f, 1f, 1f, 1f)
                    temporaryWaypointList.forEach { (player, waypoint) ->
                        waypoint(waypoint.pos, Text.translatable("firmament.waypoint.temporary", player))
                    }
                }
        }
        WorldReadyEvent.subscribe {
            temporaryWaypointList.clear()
        }
        ProcessChatEvent.subscribe {
            val matcher = temporaryWaypointMatcher.matcher(it.unformattedString)
            if (it.nameHeuristic != null && TConfig.tempWaypointDuration > 0.seconds && matcher.find()) {
                temporaryWaypointList.put(
                    it.nameHeuristic, TemporaryWaypoint(
                        BlockPos(
                            matcher.group(1).toInt(),
                            matcher.group(2).toInt(),
                            matcher.group(3).toInt(),
                        ),
                        TimeMark.now()
                    )
                )
            }
        }
    }
}

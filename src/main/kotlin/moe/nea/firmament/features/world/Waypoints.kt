/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.world

import me.shedaniel.math.Color
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.Firmament
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ClipboardUtils
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

    val temporaryPlayerWaypointList = mutableMapOf<String, TemporaryWaypoint>()
    val temporaryPlayerWaypointMatcher = "(?i)x: (-?[0-9]+),? y: (-?[0-9]+),? z: (-?[0-9]+)".toPattern()

    val waypoints = mutableListOf<BlockPos>()
    var ordered = false
    var orderedIndex = 0

    @Serializable
    data class ColeWeightWaypoint(
        val x: Int,
        val y: Int,
        val z: Int,
        val r: Int = 0,
        val g: Int = 0,
        val b: Int = 0,
    )

    override fun onLoad() {
        WorldRenderLastEvent.subscribe { event ->
            temporaryPlayerWaypointList.entries.removeIf { it.value.postedAt.passedTime() > TConfig.tempWaypointDuration }
            if (temporaryPlayerWaypointList.isNotEmpty())
                RenderInWorldContext.renderInWorld(event) {
                    color(1f, 1f, 0f, 1f)
                    temporaryPlayerWaypointList.forEach { (player, waypoint) ->
                        block(waypoint.pos)
                    }
                    color(1f, 1f, 1f, 1f)
                    temporaryPlayerWaypointList.forEach { (player, waypoint) ->
                        val skin =
                            MC.networkHandler?.listedPlayerListEntries?.find { it.profile.name == player }
                                ?.skinTextures
                                ?.texture
                        withFacingThePlayer(waypoint.pos.toCenterPos()) {
                            waypoint(waypoint.pos, Text.stringifiedTranslatable("firmament.waypoint.temporary", player))
                            if (skin != null) {
                                matrixStack.translate(0F, -20F, 0F)
                                // Head front
                                texture(
                                    skin, 16, 16,
                                    1 / 8f, 1 / 8f,
                                    2 / 8f, 2 / 8f,
                                )
                                // Head overlay
                                texture(
                                    skin, 16, 16,
                                    5 / 8f, 1 / 8f,
                                    6 / 8f, 2 / 8f,
                                )
                            }
                        }
                    }
                }
        }
        WorldReadyEvent.subscribe {
            temporaryPlayerWaypointList.clear()
        }
        CommandEvent.SubCommand.subscribe { event ->
            event.subcommand("waypoint") {
                thenArgument("pos", BlockPosArgumentType.blockPos()) { pos ->
                    thenExecute {
                        val position = pos.get(this).toAbsoluteBlockPos(source.asFakeServer())
                        waypoints.add(position)
                        source.sendFeedback(
                            Text.stringifiedTranslatable(
                                "firmament.command.waypoint.added",
                                position.x,
                                position.y,
                                position.z
                            )
                        )
                    }
                }
            }
            event.subcommand("waypoints") {
                thenLiteral("clear") {
                    thenExecute {
                        waypoints.clear()
                        source.sendFeedback(Text.translatable("firmament.command.waypoint.clear"))
                    }
                }
                thenLiteral("toggleordered") {
                    thenExecute {
                        ordered = !ordered
                        if (ordered) {
                            val p = MC.player?.pos ?: Vec3d.ZERO
                            orderedIndex =
                                waypoints.withIndex().minByOrNull { it.value.getSquaredDistance(p) }?.index ?: 0
                        }
                        source.sendFeedback(Text.translatable("firmament.command.waypoint.ordered.toggle.$ordered"))
                    }
                }
                thenLiteral("import") {
                    thenExecute {
                        val contents = ClipboardUtils.getTextContents()
                        val data = try {
                            Firmament.json.decodeFromString<List<ColeWeightWaypoint>>(contents)
                        } catch (ex: Exception) {
                            Firmament.logger.error("Could not load waypoints from clipboard", ex)
                            source.sendError(Text.translatable("firmament.command.waypoint.import.error"))
                            return@thenExecute
                        }
                        waypoints.clear()
                        data.mapTo(waypoints) { BlockPos(it.x, it.y, it.z) }
                        source.sendFeedback(
                            Text.stringifiedTranslatable(
                                "firmament.command.waypoint.import",
                                data.size
                            )
                        )
                    }
                }
            }
        }
        WorldRenderLastEvent.subscribe { event ->
            if (waypoints.isEmpty()) return@subscribe
            RenderInWorldContext.renderInWorld(event) {
                if (!ordered) {
                    color(0f, 0.3f, 0.7f, 0.5f)
                    waypoints.forEach {
                        block(it)
                    }
                } else {
                    orderedIndex %= waypoints.size
                    val firstColor = Color.ofRGBA(0, 200, 40, 180)
                    color(firstColor)
                    tracer(waypoints[orderedIndex].toCenterPos(), lineWidth = 3f)
                    waypoints.wrappingWindow(orderedIndex, 3)
                        .zip(
                            listOf(
                                firstColor,
                                Color.ofRGBA(180, 200, 40, 150),
                                Color.ofRGBA(180, 80, 20, 140),
                            )
                        )
                        .reversed()
                        .forEach { (pos, col) ->
                            color(col)
                            block(pos)
                        }
                }
            }
        }
        TickEvent.subscribe {
            if (waypoints.isEmpty() || !ordered) return@subscribe
            orderedIndex %= waypoints.size
            val p = MC.player?.pos ?: return@subscribe
            if (waypoints[orderedIndex].isWithinDistance(p, 3.0)) {
                orderedIndex = (orderedIndex + 1) % waypoints.size
            }
        }
        ProcessChatEvent.subscribe {
            val matcher = temporaryPlayerWaypointMatcher.matcher(it.unformattedString)
            if (it.nameHeuristic != null && TConfig.tempWaypointDuration > 0.seconds && matcher.find()) {
                temporaryPlayerWaypointList[it.nameHeuristic] = TemporaryWaypoint(
                    BlockPos(
                        matcher.group(1).toInt(),
                        matcher.group(2).toInt(),
                        matcher.group(3).toInt(),
                    ),
                    TimeMark.now()
                )
            }
        }
    }
}

fun <E> List<E>.wrappingWindow(startIndex: Int, windowSize: Int): List<E> {
    val result = ArrayList<E>(windowSize)
    if (startIndex + windowSize < size) {
        result.addAll(subList(startIndex, startIndex + windowSize))
    } else {
        result.addAll(subList(startIndex, size))
        result.addAll(subList(0, minOf(size - startIndex - windowSize, startIndex)))
    }
    return result
}


fun FabricClientCommandSource.asFakeServer(): ServerCommandSource {
    val source = this
    return ServerCommandSource(
        source.player,
        source.position,
        source.rotation,
        null,
        0,
        "FakeServerCommandSource",
        Text.literal("FakeServerCommandSource"),
        null,
        source.player
    )
}

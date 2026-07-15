package moe.nea.firmament.features.world

import me.shedaniel.math.Color
import kotlin.time.Duration.Companion.seconds
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.world.Waypoints.TConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.center
import moe.nea.firmament.util.render.RenderInWorldContext

object TemporaryWaypoints {
	data class TemporaryWaypoint(
        val pos: BlockPos,
        val postedAt: TimeMark,
	)
	val temporaryPlayerWaypointList = mutableMapOf<String, TemporaryWaypoint>()
	val temporaryPlayerWaypointMatcher = "(?i)x: (-?[0-9]+),? y: (-?[0-9]+),? z: (-?[0-9]+)".toPattern()
	@Subscribe
	fun onProcessChat(it: ProcessChatEvent) {
		val matcher = temporaryPlayerWaypointMatcher.matcher(it.unformattedString)
		if (it.nameHeuristic != null && TConfig.tempWaypointDuration > 0.seconds && matcher.find()) {
			temporaryPlayerWaypointList[it.nameHeuristic] = TemporaryWaypoint(BlockPos(
				matcher.group(1).toInt(),
				matcher.group(2).toInt(),
				matcher.group(3).toInt(),
			), TimeMark.now())
		}
	}
	@Subscribe
	fun onRenderTemporaryWaypoints(event: WorldRenderLastEvent) {
		temporaryPlayerWaypointList.entries.removeIf { it.value.postedAt.passedTime() > TConfig.tempWaypointDuration }
		if (temporaryPlayerWaypointList.isEmpty()) return
		RenderInWorldContext.renderInWorld(event) {
			temporaryPlayerWaypointList.forEach { (_, waypoint) ->
				block(waypoint.pos, Color.ofRGBA(255, 255, 0, 128).color)
			}
			temporaryPlayerWaypointList.forEach { (player, waypoint) ->
				val skin =
					MC.networkHandler?.listedOnlinePlayers?.find { it.profile.name == player }?.skin?.body
				withFacingThePlayer(waypoint.pos.center()) {
					waypoint(waypoint.pos, Component.translatableEscape("firmament.waypoint.temporary", player))
					if (skin != null) {
						matrixStack.translate(0F, -20F, 0F)
						// Head front
						texture(
							skin.id(), 16, 16, // TODO: 1.21.10 test
							1 / 8f, 1 / 8f,
							2 / 8f, 2 / 8f,
						)
						// Head overlay
						texture(
							skin.id(), 16, 16, // TODO: 1.21.10
							5 / 8f, 1 / 8f,
							6 / 8f, 2 / 8f,
						)
					}
				}
			}
		}
	}

	@Subscribe
	fun onWorldReady(event: WorldReadyEvent) {
		temporaryPlayerWaypointList.clear()
	}

}

package moe.nea.firmament.features.world

import com.mojang.brigadier.arguments.IntegerArgumentType
import me.shedaniel.math.Color
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.center
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.asFakeServer
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.tr

object Waypoints {
	val identifier: String
		get() = "waypoints"

	@Config
	object TConfig : ManagedConfig(identifier, Category.MINING) { // TODO: add to misc
		val tempWaypointDuration by duration("temp-waypoint-duration", 0.seconds, 1.hours) { 30.seconds }
		val showIndex by toggle("show-index") { true }
		val skipToNearest by toggle("skip-to-nearest") { false }
		val resetWaypointOrderOnWorldSwap by toggle("reset-order-on-swap") { true }
		// TODO: look ahead size
	}

	var waypoints: FirmWaypoints? = null
	var orderedIndex = 0

	@Subscribe
	fun onRenderOrderedWaypoints(event: WorldRenderLastEvent) {
		val w = useNonEmptyWaypoints() ?: return
		RenderInWorldContext.renderInWorld(event) {
			if (!w.isOrdered) {
				w.waypoints.withIndex().forEach {
					block(it.value.blockPos, Color.ofRGBA(0, 80, 160, 128).color)
					if (TConfig.showIndex) withFacingThePlayer(it.value.blockPos.center()) {
						text(Component.literal(it.index.toString()))
					}
				}
			} else {
				orderedIndex %= w.waypoints.size
				val firstColor = Color.ofRGBA(0, 200, 40, 180)
				tracer(w.waypoints[orderedIndex].blockPos.center(), color = firstColor.color, lineWidth = 3f)
				w.waypoints.withIndex().toList().wrappingWindow(orderedIndex, 3).zip(
					listOf(
						firstColor,
						Color.ofRGBA(180, 200, 40, 150),
						Color.ofRGBA(180, 80, 20, 140),
					)
				).reversed().forEach { (waypoint, col) ->
					val (index, pos) = waypoint
					block(pos.blockPos, col.color)
					if (TConfig.showIndex) withFacingThePlayer(pos.blockPos.center()) {
						text(Component.literal(index.toString()))
					}
				}
			}
		}
	}

	@Subscribe
	fun onTick(event: TickEvent) {
		val w = useNonEmptyWaypoints() ?: return
		if (!w.isOrdered) return
		orderedIndex %= w.waypoints.size
		val p = MC.player?.position() ?: return
		if (TConfig.skipToNearest) {
			orderedIndex =
				(w.waypoints.withIndex().minBy { it.value.blockPos.distToCenterSqr(p) }.index + 1) % w.waypoints.size

		} else {
			if (w.waypoints[orderedIndex].blockPos.closerToCenterThan(p, 3.0)) {
				orderedIndex = (orderedIndex + 1) % w.waypoints.size
			}
		}
	}


	fun useEditableWaypoints(): FirmWaypoints {
		var w = waypoints
		if (w == null) {
			w = FirmWaypoints("Unlabeled", "unknown", null, mutableListOf(), false)
			waypoints = w
		}
		return w
	}

	fun useNonEmptyWaypoints(): FirmWaypoints? {
		val w = waypoints
		if (w == null) return null
		if (w.waypoints.isEmpty()) return null
		return w
	}

	val WAYPOINTS_SUBCOMMAND = "waypoints"

	@Subscribe
	fun onWorldSwap(event: WorldReadyEvent) {
		if (TConfig.resetWaypointOrderOnWorldSwap) {
			orderedIndex = 0
		}
	}

	@Subscribe
	fun onCommand(event: CommandEvent.SubCommand) {
		event.subcommand("waypoint") {
			thenArgument("pos", BlockPosArgument.blockPos()) { pos ->
				thenExecute {
					source
					val position = pos.get(this).getBlockPos(source.asFakeServer())
					val w = useEditableWaypoints()
					w.waypoints.add(FirmWaypoints.Waypoint.from(position))
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.command.waypoint.added",
							position.x,
							position.y,
							position.z
						)
					)
				}
			}
		}
		event.subcommand(WAYPOINTS_SUBCOMMAND) {
			thenLiteral("reset") {
				thenExecute {
					orderedIndex = 0
					source.sendFeedback(
						tr(
							"firmament.command.waypoint.reset",
							"Reset your ordered waypoint index back to 0. If you want to delete all waypoints use /firm waypoints clear instead."
						)
					)
				}
			}
			thenLiteral("changeindex") {
				thenArgument("from", IntegerArgumentType.integer(0)) { fromIndex ->
					thenArgument("to", IntegerArgumentType.integer(0)) { toIndex ->
						thenExecute {
							val w = useEditableWaypoints()
							val toIndex = toIndex.get(this)
							val fromIndex = fromIndex.get(this)
							if (fromIndex !in w.waypoints.indices) {
								source.sendError(textInvalidIndex(fromIndex))
								return@thenExecute
							}
							if (toIndex !in w.waypoints.indices) {
								source.sendError(textInvalidIndex(toIndex))
								return@thenExecute
							}
							val waypoint = w.waypoints.removeAt(fromIndex)
							w.waypoints.add(
								if (toIndex > fromIndex) toIndex - 1
								else toIndex,
								waypoint
							)
							source.sendFeedback(
								tr(
									"firmament.command.waypoint.indexchange",
									"Moved waypoint from index $fromIndex to $toIndex. Note that this only matters for ordered waypoints."
								)
							)
						}
					}
				}
			}
			thenLiteral("clear") {
				thenExecute {
					waypoints = null
					source.sendFeedback(Component.translatable("firmament.command.waypoint.clear"))
				}
			}
			thenLiteral("toggleordered") {
				thenExecute {
					val w = useEditableWaypoints()
					w.isOrdered = !w.isOrdered
					if (w.isOrdered) {
						val p = MC.player?.position() ?: Vec3.ZERO
						orderedIndex = // TODO: this should be extracted to a utility method
							w.waypoints.withIndex().minByOrNull { it.value.blockPos.distToCenterSqr(p) }?.index ?: 0
					}
					source.sendFeedback(Component.translatable("firmament.command.waypoint.ordered.toggle.${w.isOrdered}"))
				}
			}
			thenLiteral("skip") {
				thenExecute {
					val w = useNonEmptyWaypoints()
					if (w != null && w.isOrdered) {
						orderedIndex = (orderedIndex + 1) % w.size
						source.sendFeedback(Component.translatable("firmament.command.waypoint.skip"))
					} else {
						source.sendError(Component.translatable("firmament.command.waypoint.skip.error"))
					}
				}
			}
			thenLiteral("remove") {
				thenArgument("index", IntegerArgumentType.integer(0)) { indexArg ->
					thenExecute {
						val index = get(indexArg)
						val w = useNonEmptyWaypoints()
						if (w != null && index in w.waypoints.indices) {
							w.waypoints.removeAt(index)
							source.sendFeedback(
								Component.translatableEscape(
									"firmament.command.waypoint.remove",
									index
								)
							)
						} else {
							source.sendError(Component.translatableEscape("firmament.command.waypoint.remove.error"))
						}
					}
				}
			}
		}
	}

	fun textInvalidIndex(index: Int) =
		tr(
			"firmament.command.waypoint.invalid-index",
			"Invalid index $index provided."
		)

	fun textNothingToExport(): Component =
		tr(
			"firmament.command.waypoint.export.nowaypoints",
			"No waypoints to export found. Add some with /firm waypoint ~ ~ ~."
		)
}

fun <E> List<E>.wrappingWindow(startIndex: Int, windowSize: Int): List<E> {
	val result = ArrayList<E>(windowSize)
	if (startIndex + windowSize < size) {
		result.addAll(subList(startIndex, startIndex + windowSize))
	} else {
		result.addAll(subList(startIndex, size))
		result.addAll(subList(0, minOf(windowSize - (size - startIndex), startIndex)))
	}
	return result
}

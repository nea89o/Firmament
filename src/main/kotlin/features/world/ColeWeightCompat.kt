package moe.nea.firmament.features.world

import kotlinx.serialization.Serializable
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.DefaultSource
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.tr

object ColeWeightCompat {
	@Serializable
	data class ColeWeightWaypoint(
		val x: Int?,
		val y: Int?,
		val z: Int?,
		val r: Int = 0,
		val g: Int = 0,
		val b: Int = 0,
	)

	fun fromFirm(waypoints: FirmWaypoints, relativeTo: BlockPos): List<ColeWeightWaypoint> {
		return waypoints.waypoints.map {
			ColeWeightWaypoint(it.x - relativeTo.x, it.y - relativeTo.y, it.z - relativeTo.z)
		}
	}

	fun intoFirm(waypoints: List<ColeWeightWaypoint>, relativeTo: BlockPos): FirmWaypoints {
		val w = waypoints
			.filter { it.x != null && it.y != null && it.z != null }
			.map { FirmWaypoints.Waypoint(it.x!! + relativeTo.x, it.y!! + relativeTo.y, it.z!! + relativeTo.z) }
		return FirmWaypoints(
			"Imported Waypoints",
			"imported",
			null,
			w.toMutableList(),
			false
		)
	}

	fun copyAndInform(
		source: DefaultSource,
		origin: BlockPos,
		positiveFeedback: (Int) -> Text,
	) {
		val waypoints = Waypoints.useNonEmptyWaypoints()
			?.let { fromFirm(it, origin) }
		if (waypoints == null) {
			source.sendError(Waypoints.textNothingToExport())
			return
		}
		val data =
			Firmament.tightJson.encodeToString<List<ColeWeightWaypoint>>(waypoints)
		ClipboardUtils.setTextContent(data)
		source.sendFeedback(positiveFeedback(waypoints.size))
	}

	fun importAndInform(
		source: DefaultSource,
		pos: BlockPos?,
		positiveFeedback: (Int) -> Text
	) {
		val text = ClipboardUtils.getTextContents()
		val wr = tryParse(text).map { intoFirm(it, pos ?: BlockPos.ORIGIN) }
		val waypoints = wr.getOrElse {
			source.sendError(
				tr("firmament.command.waypoint.import.cw.error",
				   "Could not import ColeWeight waypoints."))
			Firmament.logger.error(it)
			return
		}
		waypoints.lastRelativeImport = pos
		Waypoints.waypoints = waypoints
		source.sendFeedback(positiveFeedback(waypoints.size))
	}

	@Subscribe
	fun onEvent(event: CommandEvent.SubCommand) {
		event.subcommand(Waypoints.WAYPOINTS_SUBCOMMAND) {
			thenLiteral("exportcw") {
				thenExecute {
					copyAndInform(source, BlockPos.ORIGIN) {
						tr("firmament.command.waypoint.export.cw",
						   "Copied $it waypoints to clipboard in ColeWeight format.")
					}
				}
			}
			thenLiteral("exportrelativecw") {
				thenExecute {
					copyAndInform(source, MC.player?.blockPos ?: BlockPos.ORIGIN) {
						tr("firmament.command.waypoint.export.cw.relative",
						   "Copied $it relative waypoints to clipboard in ColeWeight format. Make sure to stand in the same position when importing.")
					}
				}
			}
			thenLiteral("importcw") {
				thenExecute {
					importAndInform(source, null) {
						tr("firmament.command.waypoint.import.cw.success",
							"Imported $it waypoints from ColeWeight.")
					}
				}
			}
			thenLiteral("importrelativecw") {
				thenExecute {
					importAndInform(source, MC.player!!.blockPos) {
						tr("firmament.command.waypoint.import.cw.relative",
						   "Imported $it relative waypoints from clipboard. Make sure you stand in the same position as when you exported these waypoints for them to line up correctly.")
					}
				}
			}
		}
	}

	fun tryParse(string: String): Result<List<ColeWeightWaypoint>> {
		return runCatching {
			Firmament.tightJson.decodeFromString<List<ColeWeightWaypoint>>(string)
		}
	}
}

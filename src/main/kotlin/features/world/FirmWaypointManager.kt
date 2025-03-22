package moe.nea.firmament.features.world

import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.serialization.serializer
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.DefaultSource
import moe.nea.firmament.commands.RestArgumentType
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.suggestsList
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TemplateUtil
import moe.nea.firmament.util.data.MultiFileDataHolder
import moe.nea.firmament.util.tr

object FirmWaypointManager {
	object DataHolder : MultiFileDataHolder<FirmWaypoints>(serializer(), "waypoints")

	val SHARE_PREFIX = "FIRM_WAYPOINTS/"
	val ENCODED_SHARE_PREFIX = TemplateUtil.getPrefixComparisonSafeBase64Encoding(SHARE_PREFIX)

	fun createExportableCopy(
		waypoints: FirmWaypoints,
	): FirmWaypoints {
		val copy = waypoints.copy(waypoints = waypoints.waypoints.toMutableList())
		if (waypoints.isRelativeTo != null) {
			val origin = waypoints.lastRelativeImport
			if (origin != null) {
				copy.waypoints.replaceAll {
					it.copy(
						x = it.x - origin.x,
						y = it.y - origin.y,
						z = it.z - origin.z,
					)
				}
			} else {
				TODO("Add warning!")
			}
		}
		return copy
	}

	fun loadWaypoints(waypoints: FirmWaypoints, sendFeedback: (Text) -> Unit) {
		val copy = waypoints.deepCopy()
		if (copy.isRelativeTo != null) {
			val origin = MC.player!!.blockPos
			copy.waypoints.replaceAll {
				it.copy(
					x = it.x + origin.x,
					y = it.y + origin.y,
					z = it.z + origin.z,
				)
			}
			copy.lastRelativeImport = origin.toImmutable()
			sendFeedback(tr("firmament.command.waypoint.import.ordered.success",
			                "Imported ${copy.size} relative waypoints. Make sure you stand in the correct spot while loading the waypoints: ${copy.isRelativeTo}."))
		} else {
			sendFeedback(tr("firmament.command.waypoint.import.success",
			                "Imported ${copy.size} waypoints."))
		}
		Waypoints.waypoints = copy
	}

	fun setOrigin(source: DefaultSource, text: String?) {
		val waypoints = Waypoints.useEditableWaypoints()
		waypoints.isRelativeTo = text ?: waypoints.isRelativeTo ?: ""
		val pos = MC.player!!.blockPos
		waypoints.lastRelativeImport = pos
		source.sendFeedback(tr("firmament.command.waypoint.originset",
		                       "Set the origin of waypoints to ${FirmFormatters.formatPosition(pos)}. Run /firm waypoints export to save the waypoints relative to this position."))
	}

	@Subscribe
	fun onCommands(event: CommandEvent.SubCommand) {
		event.subcommand(Waypoints.WAYPOINTS_SUBCOMMAND) {
			thenLiteral("setorigin") {
				thenExecute {
					setOrigin(source, null)
				}
				thenArgument("hint", RestArgumentType) { text ->
					thenExecute {
						setOrigin(source, this[text])
					}
				}
			}
			thenLiteral("clearorigin") {
				thenExecute {
					val waypoints = Waypoints.useEditableWaypoints()
					waypoints.lastRelativeImport = null
					waypoints.isRelativeTo = null
					source.sendFeedback(tr("firmament.command.waypoint.originunset",
					                       "Unset the origin of the waypoints. Run /firm waypoints export to save the waypoints with absolute coordinates."))
				}
			}
			thenLiteral("save") {
				thenArgument("name", StringArgumentType.string()) { name ->
					suggestsList { DataHolder.list().keys }
					thenExecute {
						val waypoints = Waypoints.useNonEmptyWaypoints()
						if (waypoints == null) {
							source.sendError(Waypoints.textNothingToExport())
							return@thenExecute
						}
						waypoints.id = get(name)
						val exportableWaypoints = createExportableCopy(waypoints)
						DataHolder.insert(get(name), exportableWaypoints)
						DataHolder.save()
						source.sendFeedback(tr("firmament.command.waypoint.saved",
						                       "Saved waypoints locally as ${get(name)}. Use /firm waypoints load to load them again."))
					}
				}
			}
			thenLiteral("load") {
				thenArgument("name", StringArgumentType.string()) { name ->
					suggestsList { DataHolder.list().keys }
					thenExecute {
						val name = get(name)
						val waypoints = DataHolder.list()[name]
						if (waypoints == null) {
							source.sendError(
								tr("firmament.command.waypoint.nosaved",
								   "No saved waypoint for ${name}. Use tab completion to see available names."))
							return@thenExecute
						}
						loadWaypoints(waypoints, source::sendFeedback)
					}
				}
			}
			thenLiteral("export") {
				thenExecute {
					val waypoints = Waypoints.useNonEmptyWaypoints()
					if (waypoints == null) {
						source.sendError(Waypoints.textNothingToExport())
						return@thenExecute
					}
					val exportableWaypoints = createExportableCopy(waypoints)
					val data = TemplateUtil.encodeTemplate(SHARE_PREFIX, exportableWaypoints)
					ClipboardUtils.setTextContent(data)
					source.sendFeedback(tr("firmament.command.waypoint.export",
					                       "Copied ${exportableWaypoints.size} waypoints to clipboard in Firmament format."))
				}
			}
			thenLiteral("import") {
				thenExecute {
					val text = ClipboardUtils.getTextContents()
					if (text.startsWith("[")) {
						source.sendError(tr("firmament.command.waypoint.import.lookslikecw",
						                    "The waypoints in your clipboard look like they might be ColeWeight waypoints. If so, use /firm waypoints importcw or /firm waypoints importrelativecw."))
						return@thenExecute
					}
					val waypoints = TemplateUtil.maybeDecodeTemplate<FirmWaypoints>(SHARE_PREFIX, text)
					if (waypoints == null) {
						source.sendError(tr("firmament.command.waypoint.import.error",
						                    "Could not import Firmament waypoints from your clipboard. Make sure they are Firmament compatible waypoints."))
						return@thenExecute
					}
					loadWaypoints(waypoints, source::sendFeedback)
				}
			}
		}
	}
}

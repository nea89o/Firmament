package moe.nea.firmament.features.chat

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlin.time.Duration.Companion.seconds
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.CaseInsensitiveLiteralCommandNode
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.PartyMessageReceivedEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.useMatch

object PartyCommands {

	val messageInChannel = "(?<channel>Party|Guild) >([^:]+?)? (?<name>[^: ]+): (?<message>.+)".toPattern()

	@Subscribe
	fun onChat(event: ProcessChatEvent) {
		messageInChannel.useMatch(event.unformattedString) {
			val channel = group("channel")
			val message = group("message")
			val name = group("name")
			if (channel == "Party") {
				PartyMessageReceivedEvent.publish(PartyMessageReceivedEvent(
					event, message, name
				))
			}
		}
	}

	val commandPrefixes = "!-?$.&#+~€\"@°_;:³²`'´ß\\,|".toSet()

	data class PartyCommandContext(
		val name: String
	)

	val dispatch = CommandDispatcher<PartyCommandContext>().also { dispatch ->
		fun register(
			name: String,
			vararg alias: String,
			block: CaseInsensitiveLiteralCommandNode.Builder<PartyCommandContext>.() -> Unit = {},
		): LiteralCommandNode<PartyCommandContext> {
			val node =
				dispatch.register(CaseInsensitiveLiteralCommandNode.Builder<PartyCommandContext>(name).also(block))
			alias.forEach { register(it) { redirect(node) } }
			return node
		}

		register("warp", "pw", "pwarp", "partywarp") {
			executes {
				// TODO: add check if you are the party leader
				MC.sendCommand("p warp")
				0
			}
		}

		register("transfer", "pt", "ptme") {
			executes {
				MC.sendCommand("p transfer ${it.source.name}")
				0
			}
		}

		register("allinvite", "allinv") {
			executes {
				MC.sendCommand("p settings allinvite")
				0
			}
		}

		register("coords") {
			executes {
				val p = MC.player?.blockPos ?: BlockPos.ORIGIN
				MC.sendCommand("pc x: ${p.x}, y: ${p.y}, z: ${p.z}")
				0
			}
		}
		// TODO: downtime tracker (display message again at end of dungeon)
		// instance ends: kuudra, dungeons, bacte
		// TODO: at TPS command
	}

	object TConfig : ManagedConfig("party-commands", Category.CHAT) {
		val enable by toggle("enable") { false }
		val cooldown by duration("cooldown", 0.seconds, 20.seconds) { 2.seconds }
		val ignoreOwnCommands by toggle("ignore-own") { false }
	}

	var lastCommand = TimeMark.farPast()

	@Subscribe
	fun listPartyCommands(event: CommandEvent.SubCommand) {
		event.subcommand("partycommands") {
			thenExecute {
				// TODO: Better help, including descriptions and redirect detection
				MC.sendChat(tr("firmament.partycommands.help", "Available party commands: ${dispatch.root.children.map { it.name }}. Available prefixes: $commandPrefixes"))
			}
		}
	}

	@Subscribe
	fun onPartyMessage(event: PartyMessageReceivedEvent) {
		if (!TConfig.enable) return
		if (event.message.firstOrNull() !in commandPrefixes) return
		if (event.name == MC.playerName && TConfig.ignoreOwnCommands) return
		if (lastCommand.passedTime() < TConfig.cooldown) {
			MC.sendChat(tr("firmament.partycommands.cooldown", "Skipping party command. Cooldown not passed."))
			return
		}
		// TODO: add trust levels
		val commandLine = event.message.substring(1)
		try {
			dispatch.execute(StringReader(commandLine), PartyCommandContext(event.name))
		} catch (ex: Exception) {
			if (ex is CommandSyntaxException) {
				MC.sendChat(tr("firmament.partycommands.unknowncommand", "Unknown party command."))
				return
			} else {
				MC.sendChat(tr("firmament.partycommands.unknownerror", "Unknown error during command execution."))
				ErrorUtil.softError("Unknown error during command execution.", ex)
			}
		}
		lastCommand = TimeMark.now()
	}
}

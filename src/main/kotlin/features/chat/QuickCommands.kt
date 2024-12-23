package moe.nea.firmament.features.chat

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.DefaultSource
import moe.nea.firmament.commands.RestArgumentType
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.tr

object QuickCommands : FirmamentFeature {
	override val identifier: String
		get() = "quick-commands"

	object TConfig : ManagedConfig("quick-commands", Category.CHAT) {
		val enableJoin by toggle("join") { true }
		val enableDh by toggle("dh") { true }
		override fun onChange(option: ManagedOption<*>) {
			reloadCommands()
		}
	}

	fun reloadCommands() {
		val lastPacket = lastReceivedTreePacket ?: return
		val network = MC.networkHandler ?: return
		val fallback = ClientCommandInternals.getActiveDispatcher()
		try {
			val dispatcher = CommandDispatcher<FabricClientCommandSource>()
			ClientCommandInternals.setActiveDispatcher(dispatcher)
			ClientCommandRegistrationCallback.EVENT.invoker()
				.register(dispatcher, CommandRegistryAccess.of(network.combinedDynamicRegistries,
				                                               network.enabledFeatures))
			ClientCommandInternals.finalizeInit()
			network.onCommandTree(lastPacket)
		} catch (ex: Exception) {
			ClientCommandInternals.setActiveDispatcher(fallback)
			throw ex
		}
	}


	fun removePartialPrefix(text: String, prefix: String): String? {
		var lf: String? = null
		for (i in 1..prefix.length) {
			if (text.startsWith(prefix.substring(0, i))) {
				lf = text.substring(i)
			}
		}
		return lf
	}

	var lastReceivedTreePacket: CommandTreeS2CPacket? = null

	val kuudraLevelNames = listOf("NORMAL", "HOT", "BURNING", "FIERY", "INFERNAL")
	val dungeonLevelNames = listOf("ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN")

	@Subscribe
	fun registerDh(event: CommandEvent) {
		if (!TConfig.enableDh) return
		event.register("dh") {
			thenExecute {
				MC.sendCommand("warp dhub")
			}
		}
		event.register("dn") {
			thenExecute {
				MC.sendChat(tr("firmament.quickwarp.deez-nutz", "Warping to... Deez Nuts!").grey())
				MC.sendCommand("warp dhub")
			}
		}
	}

	@Subscribe
	fun registerJoin(it: CommandEvent) {
		if (!TConfig.enableJoin) return
		it.register("join") {
			thenArgument("what", RestArgumentType) { what ->
				thenExecute {
					val what = this[what]
					if (!SBData.isOnSkyblock) {
						MC.sendCommand("join $what")
						return@thenExecute
					}
					val joinName = getNameForFloor(what.replace(" ", "").lowercase())
					if (joinName == null) {
						source.sendFeedback(Text.stringifiedTranslatable("firmament.quick-commands.join.unknown", what))
					} else {
						source.sendFeedback(Text.stringifiedTranslatable("firmament.quick-commands.join.success",
						                                                 joinName))
						MC.sendCommand("joininstance $joinName")
					}
				}
			}
			thenExecute {
				source.sendFeedback(Text.translatable("firmament.quick-commands.join.explain"))
			}
		}
	}

	fun CommandContext<DefaultSource>.getNameForFloor(w: String): String? {
		val kuudraLevel = removePartialPrefix(w, "kuudratier") ?: removePartialPrefix(w, "tier")
		if (kuudraLevel != null) {
			val l = kuudraLevel.toIntOrNull()?.let { it - 1 } ?: kuudraLevelNames.indexOfFirst {
				it.startsWith(
					kuudraLevel,
					true
				)
			}
			if (l !in kuudraLevelNames.indices) {
				source.sendFeedback(Text.stringifiedTranslatable("firmament.quick-commands.join.unknown-kuudra",
				                                                 kuudraLevel))
				return null
			}
			return "KUUDRA_${kuudraLevelNames[l]}"
		}
		val masterLevel = removePartialPrefix(w, "master")
		val normalLevel =
			removePartialPrefix(w, "floor") ?: removePartialPrefix(w, "catacombs") ?: removePartialPrefix(w, "dungeons")
		val dungeonLevel = masterLevel ?: normalLevel
		if (dungeonLevel != null) {
			val l = dungeonLevel.toIntOrNull()?.let { it - 1 } ?: dungeonLevelNames.indexOfFirst {
				it.startsWith(
					dungeonLevel,
					true
				)
			}
			if (masterLevel == null && (l == -1 || null != removePartialPrefix(w, "entrance"))) {
				return "CATACOMBS_ENTRANCE"
			}
			if (l !in dungeonLevelNames.indices) {
				source.sendFeedback(Text.stringifiedTranslatable("firmament.quick-commands.join.unknown-catacombs",
				                                                 kuudraLevel))
				return null
			}
			return "${if (masterLevel != null) "MASTER_" else ""}CATACOMBS_FLOOR_${dungeonLevelNames[l]}"
		}
		return null
	}
}

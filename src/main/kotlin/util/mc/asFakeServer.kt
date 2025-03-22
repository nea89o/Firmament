package moe.nea.firmament.util.mc

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

fun FabricClientCommandSource.asFakeServer(): ServerCommandSource {
	val source = this
	return ServerCommandSource(
		object : CommandOutput {
			override fun sendMessage(message: Text?) {
				source.player.sendMessage(message, false)
			}

			override fun shouldReceiveFeedback(): Boolean {
				return true
			}

			override fun shouldTrackOutput(): Boolean {
				return true
			}

			override fun shouldBroadcastConsoleToOps(): Boolean {
				return true
			}
		},
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

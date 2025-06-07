package moe.nea.firmament.features.debug

import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.SoundReceiveEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.red
import moe.nea.firmament.util.render.RenderInWorldContext

object SoundVisualizer {

	var showSounds = false

	var sounds = mutableListOf<SoundReceiveEvent>()


	@Subscribe
	fun onSubCommand(event: CommandEvent.SubCommand) {
		event.subcommand("dev") {
			thenLiteral("sounds") {
				thenExecute {
					showSounds = !showSounds
					if (!showSounds) {
						sounds.clear()
					}
				}
			}
		}
	}

	@Subscribe
	fun onWorldSwap(event: WorldReadyEvent) {
		sounds.clear()
	}

	@Subscribe
	fun onRender(event: WorldRenderLastEvent) {
		RenderInWorldContext.renderInWorld(event) {
			sounds.forEach { event ->
				withFacingThePlayer(event.position) {
					text(
						Text.literal(event.sound.value().id.toString()).also {
							if (event.cancelled)
								it.red()
						},
						verticalAlign = RenderInWorldContext.VerticalAlign.CENTER,
					)
				}
			}
		}
	}

	@Subscribe
	fun onSoundReceive(event: SoundReceiveEvent) {
		if (!showSounds) return
		if (sounds.size > 1000) {
			sounds.subList(0, 200).clear()
		}
		sounds.add(event)
	}
}

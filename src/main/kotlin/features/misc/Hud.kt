package moe.nea.firmament.features.misc

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.tr
import moe.nea.jarvis.api.Point
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text

object Hud : FirmamentFeature {
	override val identifier: String
		get() = "hud"

	object TConfig : ManagedConfig(identifier, Category.MISC) {
		var dayCount by toggle("day-count") { false }
		val dayCountHud by position("day-count-hud", 80, 10) { Point(0.5, 0.8) }
		var fpsCount by toggle("fps-count") { false }
		val fpsCountHud by position("fps-count-hud", 80, 10) { Point(0.5, 0.9) }
		var pingCount by toggle("ping-count") { false }
		val pingCountHud by position("ping-count-hud", 80, 10) { Point(0.5, 1.0) }
	}

	override val config: ManagedConfig
		get() = TConfig

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (TConfig.dayCount) {
			it.context.matrices.push()
			TConfig.dayCountHud.applyTransformations(it.context.matrices)
			val day = (MC.world?.timeOfDay ?: 0L) / 24000
			it.context.drawText(
				MC.font,
				Text.literal(String.format(tr("firmament.config.hud.day-count-hud.display", "Day: %s").string, day)),
				36,
				MC.font.fontHeight,
				-1,
				true
			)
			it.context.matrices.pop()
		}

		if (TConfig.fpsCount) {
			it.context.matrices.push()
			TConfig.fpsCountHud.applyTransformations(it.context.matrices)
			it.context.drawText(
				MC.font, Text.literal(
					String.format(
						tr("firmament.config.hud.fps-count-hud.display", "FPS: %s").string, MC.instance.currentFps
					)
				), 36, MC.font.fontHeight, -1, true
			)
			it.context.matrices.pop()
		}

		if (TConfig.pingCount) {
			it.context.matrices.push()
			TConfig.pingCountHud.applyTransformations(it.context.matrices)
			val ping = MC.player?.let {
				val entry: PlayerListEntry? = MC.networkHandler?.getPlayerListEntry(it.uuid)
				entry?.latency ?: -1
			} ?: -1
			it.context.drawText(
				MC.font, Text.literal(
					String.format(
						tr("firmament.config.hud.ping-count-hud.display", "Ping: %s ms").string, ping
					)
				), 36, MC.font.fontHeight, -1, true
			)

			it.context.matrices.pop()
		}
	}
}

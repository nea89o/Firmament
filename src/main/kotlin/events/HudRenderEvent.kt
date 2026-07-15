package moe.nea.firmament.events

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.DeltaTracker
import net.minecraft.world.level.GameType
import moe.nea.firmament.util.MC

/**
 * Called when hud elements should be rendered, before the screen, but after the world.
 */
data class HudRenderEvent(val context: GuiGraphicsExtractor, val tickDelta: DeltaTracker) : FirmamentEvent.Cancellable() {
	val isRenderingHud = !MC.inGameHud.isHidden
	val isRenderingCursor = MC.interactionManager?.playerMode != GameType.SPECTATOR && isRenderingHud

	init {
		if (!isRenderingHud)
			cancel()
	}

	companion object : FirmamentEventBus<HudRenderEvent>()
}

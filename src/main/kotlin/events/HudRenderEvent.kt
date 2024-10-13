

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.world.GameMode
import moe.nea.firmament.util.MC

/**
 * Called when hud elements should be rendered, before the screen, but after the world.
 */
data class HudRenderEvent(val context: DrawContext, val tickDelta: RenderTickCounter) : FirmamentEvent() {
	val isRenderingHud = !MC.options.hudHidden
	val isRenderingCursor = MC.interactionManager?.currentGameMode != GameMode.SPECTATOR && isRenderingHud
    companion object : FirmamentEventBus<HudRenderEvent>()
}

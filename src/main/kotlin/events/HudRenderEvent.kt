

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter

/**
 * Called when hud elements should be rendered, before the screen, but after the world.
 */
data class HudRenderEvent(val context: DrawContext, val tickDelta: RenderTickCounter) : FirmamentEvent() {
    companion object : FirmamentEventBus<HudRenderEvent>()
}

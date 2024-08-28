

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen

data class HandledScreenForegroundEvent(
    val screen: HandledScreen<*>,
    val context: DrawContext,
    val mouseX: Int,
    val mouseY: Int,
    val delta: Float
) : FirmamentEvent() {
    companion object : FirmamentEventBus<HandledScreenForegroundEvent>()
}

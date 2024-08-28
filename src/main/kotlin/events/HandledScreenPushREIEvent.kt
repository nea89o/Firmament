

package moe.nea.firmament.events

import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.screen.ingame.HandledScreen

data class HandledScreenPushREIEvent(
    val screen: HandledScreen<*>,
    val rectangles: MutableList<Rectangle> = mutableListOf()
) : FirmamentEvent() {

    fun block(rectangle: Rectangle) {
        rectangles.add(rectangle)
    }

    companion object : FirmamentEventBus<HandledScreenPushREIEvent>()
}

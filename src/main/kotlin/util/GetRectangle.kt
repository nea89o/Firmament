

package moe.nea.firmament.util

import me.shedaniel.math.Rectangle
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen

fun HandledScreen<*>.getRectangle(): Rectangle {
    this as AccessorHandledScreen
    return Rectangle(
        getX_Firmament(),
        getY_Firmament(),
        getBackgroundWidth_Firmament(),
        getBackgroundHeight_Firmament()
    )
}

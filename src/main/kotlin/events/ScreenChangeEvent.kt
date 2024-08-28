

package moe.nea.firmament.events

import net.minecraft.client.gui.screen.Screen

data class ScreenChangeEvent(val old: Screen?, val new: Screen?) : FirmamentEvent.Cancellable() {
    var overrideScreen: Screen? = null
    companion object : FirmamentEventBus<ScreenChangeEvent>()
}

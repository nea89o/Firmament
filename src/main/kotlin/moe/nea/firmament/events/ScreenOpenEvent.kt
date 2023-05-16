package moe.nea.firmament.events

import net.minecraft.client.gui.screen.Screen

data class ScreenOpenEvent(val old: Screen?, val new: Screen?) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<ScreenOpenEvent>()
}

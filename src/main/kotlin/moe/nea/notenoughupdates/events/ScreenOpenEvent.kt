package moe.nea.notenoughupdates.events

import net.minecraft.client.gui.screen.Screen

data class ScreenOpenEvent(val old: Screen?, val new: Screen?) : NEUEvent.Cancellable() {
    companion object : NEUEventBus<ScreenOpenEvent>()
}

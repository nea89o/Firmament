package moe.nea.firmament.events

import net.minecraft.client.gui.screen.ingame.HandledScreen

data class ScreenClickEvent(val screen: HandledScreen<*>, val mouseX: Double, val mouseY: Double, val button: Int) :
    FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<ScreenClickEvent>()
}

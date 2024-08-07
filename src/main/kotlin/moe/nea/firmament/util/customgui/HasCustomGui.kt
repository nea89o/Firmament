
package moe.nea.firmament.util.customgui

import net.minecraft.client.gui.screen.ingame.HandledScreen

@Suppress("FunctionName")
interface HasCustomGui {
    fun getCustomGui_Firmament(): CustomGui?
    fun setCustomGui_Firmament(gui: CustomGui?)
}

var <T : HandledScreen<*>> T.customGui: CustomGui?
    get() = (this as HasCustomGui).getCustomGui_Firmament()
    set(value) {
        (this as HasCustomGui).setCustomGui_Firmament(value)
    }


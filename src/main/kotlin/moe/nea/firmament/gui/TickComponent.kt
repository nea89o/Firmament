package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext

class TickComponent(val onTick: Runnable) : GuiComponent() {
    override fun getWidth(): Int {
        return 0
    }

    override fun getHeight(): Int {
        return 0
    }

    override fun render(context: GuiImmediateContext) {
        onTick.run()
    }
}

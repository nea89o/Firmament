package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WScrollPanel
import io.github.cottonmc.cotton.gui.widget.WWidget

class WTightScrollPanel(val widget: WWidget, val margin: Int = 3) : WScrollPanel(widget) {
    override fun setSize(x: Int, y: Int) {
        (widget as? WPanel)?.layout()
        super.setSize(widget.width + 8 + margin, y)
    }
}

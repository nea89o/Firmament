package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget

class WSpacer(val child: WWidget, val spaceLeft: Int) : WPanel() {
    init {
        children.add(child)
        child.setLocation(spaceLeft, 0)
    }

    override fun getWidth(): Int {
        return child.width + spaceLeft
    }

    override fun getHeight(): Int {
        return child.height
    }
}

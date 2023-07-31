package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets
import io.github.cottonmc.cotton.gui.widget.WWidget

class WSplitPanel(val left: WWidget, val right: WWidget) : WPanelWithInsets() {
    init {
        left.parent = this
        right.parent = this
        children.add(left)
        children.add(right)
    }

    override fun layout() {
        expandToFit(left, insets)
        expandToFit(right, insets)
        (left as? WPanel)?.layout()
        (right as? WPanel)?.layout()
        left.setLocation(insets.left, insets.top)
        right.setLocation(width - insets.right - right.width, insets.top)
    }
}

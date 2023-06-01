package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis

data class WCenteringPanel(
    val child: WWidget,
    val axis: Axis,
) : WPanel() {
    init {
        child.parent = this
    }

    override fun setSize(x: Int, y: Int) {
        super.setSize(x, y)
        if (!child.canResize()) return
        if (axis == Axis.HORIZONTAL) {
            child.setSize(child.width, y)
        } else {
            child.setSize(x, child.height)
        }
    }

    override fun layout() {
        super.layout()
        child.setLocation(
            axis.choose((child.width + width) / 2, child.x),
            axis.choose(child.y, (child.height + height) / 2),
        )
    }


}

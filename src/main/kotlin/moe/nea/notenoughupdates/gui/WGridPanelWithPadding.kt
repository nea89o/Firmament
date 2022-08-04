package moe.nea.notenoughupdates.gui

import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets

class WGridPanelWithPadding(
    val grid: Int = 18,
    val verticalPadding: Int = 0,
    val horizontalPadding: Int = 0,
) : WPanelWithInsets() {

    private inline val vertOffset get() = grid + verticalPadding
    private inline val horiOffset get() = grid + horizontalPadding

    fun add(w: WWidget, x: Int, y: Int, width: Int = 1, height: Int = 1) {
        children.add(w)
        w.parent = this
        w.setLocation(x * horiOffset + insets.left, y * vertOffset + insets.top)
        if (w.canResize())
            w.setSize(
                grid + (horiOffset * (width - 1)),
                grid + (vertOffset * (height - 1)),
            )
        expandToFit(w, insets)
    }

    override fun setInsets(insets: Insets): WGridPanelWithPadding {
        super.setInsets(insets)
        return this
    }

}
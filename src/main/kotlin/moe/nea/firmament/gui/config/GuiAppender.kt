package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import net.minecraft.text.Text

class GuiAppender(val width: Int) {
    private var row = 0
    internal val panel = WGridPanel().also { it.setGaps(4, 4) }
    internal val reloadables = mutableListOf<(() -> Unit)>()
    fun set(x: Int, y: Int, w: Int, h: Int, widget: WWidget) {
        panel.add(widget, x, y, w, h)
    }


    fun onReload(reloadable: () -> Unit) {
        reloadables.add(reloadable)
    }

    fun skipRows(r: Int) {
        row += r
    }

    fun appendLabeledRow(label: Text, right: WWidget) {
        appendSplitRow(
            WLabel(label).setVerticalAlignment(VerticalAlignment.CENTER),
            right
        )
    }

    fun appendSplitRow(left: WWidget, right: WWidget) {
        val lw = width / 2
        set(0, row, lw, 1, left)
        set(lw, row, width - lw, 1, right)
        skipRows(1)
    }

    fun appendFullRow(widget: WWidget) {
        set(0, row, width, 1, widget)
        skipRows(1)
    }
}

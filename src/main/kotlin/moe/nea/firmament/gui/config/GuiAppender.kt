/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        panel.add(widget, x, y + row, w, h)
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
        set(0, 0, lw, 1, left)
        set(lw, 0, width - lw, 1, right)
        skipRows(1)
    }

    fun appendFullRow(widget: WWidget) {
        set(0, 0, width, 1, widget)
        skipRows(1)
    }
}

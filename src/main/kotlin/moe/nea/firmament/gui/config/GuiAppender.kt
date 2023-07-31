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

import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.gui.WFixedPanel
import moe.nea.firmament.gui.WSplitPanel

class GuiAppender(val width: Int, val screenAccessor: () -> Screen) {
    val panel = WBox(Axis.VERTICAL).also {
        it.setSize(width, 200)
    }
    internal val reloadables = mutableListOf<(() -> Unit)>()

    fun onReload(reloadable: () -> Unit) {
        reloadables.add(reloadable)
    }

    fun appendLabeledRow(label: Text, right: WWidget) {
        appendSplitRow(
            WLabel(label).setVerticalAlignment(VerticalAlignment.CENTER),
            right
        )
    }

    fun appendSplitRow(left: WWidget, right: WWidget) {
        appendFullRow(WSplitPanel(left.also { it.setSize(width / 2, 18) }, right.also { it.setSize(width / 2, 18) }))
    }

    fun appendFullRow(widget: WWidget) {
        panel.add(widget, width, 18)
    }
}

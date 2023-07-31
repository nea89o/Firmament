/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
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

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

package moe.nea.firmament.hud

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.roundToInt
import kotlin.math.sin
import net.minecraft.client.gui.DrawContext


val Insets.vertical get() = bottom + top
val Insets.horizontal get() = left + right

class ProgressBar(
    var label: String,
    var total: Int?, // If total is null, then make it a bouncy rectangle
    var progress: Int = 0,
) : WWidget() {

    var insets: Insets = Insets(7)
    override fun canResize(): Boolean = true


    fun reportProgress(label: String, progress: Int, total: Int?) {
        synchronized(this) {
            this.label = label
            this.progress = progress
            this.total = total
        }

    }

    override fun paint(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.coloredRect(context, x, y, width, height, 0xFF808080.toInt())
        val (l, prog) = synchronized(this) {
            label to (progress to total)
        }
        val (p, t) = prog

        if (t == null) {
            ScreenDrawing.coloredRect(
                context,
                (x + (1 + sin(System.currentTimeMillis().toDouble() / 1000)) * width * 3 / 4 / 2).roundToInt(),
                y,
                width / 4,
                height,
                0xFF00FF00.toInt()
            )
        } else {
            ScreenDrawing.coloredRect(context, x, y, width * p / t, height, 0xFF00FF00.toInt())
        }
        ScreenDrawing.drawString(
            context,
            if (t != null) "$l ($p/$t)" else l,
            HorizontalAlignment.CENTER,
            x + insets.left,
            y + insets.top,
            width - insets.horizontal,
            height - insets.vertical,
        )
    }
}

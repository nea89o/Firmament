package moe.nea.notenoughupdates.hud

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.roundToInt
import kotlin.math.sin


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

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.coloredRect(matrices, x, y, width, height, 0xFF808080.toInt())
        val (l, prog) = synchronized(this) {
            label to (progress to total)
        }
        val (p, t) = prog

        if (t == null) {
            ScreenDrawing.coloredRect(
                matrices,
                (x + (1 + sin(System.currentTimeMillis().toDouble() / 1000)) * width * 3 / 4 / 2).roundToInt(),
                y,
                width / 4,
                height,
                0xFF00FF00.toInt()
            )
        } else {
            ScreenDrawing.coloredRect(matrices, x, y, width * p / t, height, 0xFF00FF00.toInt())
        }
        ScreenDrawing.drawString(
            matrices,
            if (t != null) "$l ($p/$t)" else l,
            HorizontalAlignment.CENTER,
            x + insets.left,
            y + insets.top,
            width - insets.horizontal,
            height - insets.vertical,
        )
    }
}

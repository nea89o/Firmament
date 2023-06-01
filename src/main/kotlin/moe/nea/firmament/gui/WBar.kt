package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Texture
import me.shedaniel.math.Color
import net.minecraft.client.util.math.MatrixStack
import moe.nea.firmament.Firmament

data class WBar(
    var progress: Double,
    val total: Double,
    val fillColor: Color,
    val emptyColor: Color,
) : WWidget() {
    val resource = Firmament.identifier("textures/gui/bar.png")
    val left = Texture(resource, 0 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
    val middle = Texture(resource, 4 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
    val right = Texture(resource, 8 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
    val segmentOverlay = Texture(resource, 12 / 64F, 0 / 64F, 15 / 64F, 8 / 64F)

    override fun canResize(): Boolean {
        return true
    }

    private fun drawSection(
        matrices: MatrixStack,
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        sectionStart: Double,
        sectionEnd: Double
    ) {
        if (sectionEnd < progress && width == 4) {
            ScreenDrawing.texturedRect(matrices, x, y, 4, 8, texture, fillColor.color)
            return
        }
        if (sectionStart > progress && width == 4) {
            ScreenDrawing.texturedRect(matrices, x, y, 4, 8, texture, emptyColor.color)
            return
        }
        val increasePerPixel = (sectionEnd - sectionStart / 4)
        var valueAtPixel = sectionStart
        for (i in (0 until width)) {
            ScreenDrawing.texturedRect(
                matrices, x + i, y, 1, 8,
                texture.image, texture.u1 + i / 64F, texture.v1, texture.u1 + (i + 1) / 64F, texture.v2,
                if (valueAtPixel < progress) fillColor.color else emptyColor.color
            )
            valueAtPixel += increasePerPixel
        }
    }

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        var i = 0
        while (i < width - 4) {
            drawSection(
                matrices,
                if (i == 0) left else middle,
                x + i, y,
                (width - (i + 4)).coerceAtMost(4),
                i * total / width, (i + 4) * total / width
            )
            i += 4
        }
        drawSection(matrices, right, x + width - 4, y, 4, (width - 4) * total / width, total)
    }
}

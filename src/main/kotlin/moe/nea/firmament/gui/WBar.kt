package moe.nea.firmament.gui

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Texture
import me.shedaniel.math.Color
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import moe.nea.firmament.Firmament

open class WBar(
    var progress: Double,
    val total: Double,
    val fillColor: Color,
    val emptyColor: Color,
) : WWidget() {
    companion object {
        val resource = Firmament.identifier("textures/gui/bar.png")
        val left = Texture(resource, 0 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
        val middle = Texture(resource, 4 / 64F, 0 / 64F, 8 / 64F, 8 / 64F)
        val right = Texture(resource, 8 / 64F, 0 / 64F, 12 / 64F, 8 / 64F)
        val segmentOverlay = Texture(resource, 12 / 64F, 0 / 64F, 15 / 64F, 8 / 64F)
    }

    override fun canResize(): Boolean {
        return true
    }

    private fun drawSection(
        context: DrawContext,
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        sectionStart: Double,
        sectionEnd: Double
    ) {
        if (sectionEnd < progress && width == 4) {
            ScreenDrawing.texturedRect(context, x, y, 4, 8, texture, fillColor.color)
            return
        }
        if (sectionStart > progress && width == 4) {
            ScreenDrawing.texturedRect(context, x, y, 4, 8, texture, emptyColor.color)
            return
        }
        val increasePerPixel = (sectionEnd - sectionStart / 4)
        var valueAtPixel = sectionStart
        for (i in (0 until width)) {
            ScreenDrawing.texturedRect(
                context, x + i, y, 1, 8,
                texture.image, texture.u1 + i / 64F, texture.v1, texture.u1 + (i + 1) / 64F, texture.v2,
                if (valueAtPixel < progress) fillColor.color else emptyColor.color
            )
            valueAtPixel += increasePerPixel
        }
    }

    override fun paint(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        var i = 0
        while (i < width - 4) {
            drawSection(
                context,
                if (i == 0) left else middle,
                x + i, y,
                (width - (i + 4)).coerceAtMost(4),
                i * total / width, (i + 4) * total / width
            )
            i += 4
        }
        drawSection(context, right, x + width - 4, y, 4, (width - 4) * total / width, total)
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
    }
}

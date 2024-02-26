/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.data.Texture
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.common.RenderContext
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.platform.ModernRenderContext
import me.shedaniel.math.Color
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament

class BarComponent(
    val progress: GetSetter<Double>, val total: GetSetter<Double>,
    val fillColor: Color,
    val emptyColor: Color,
) : GuiComponent() {
    override fun getWidth(): Int {
        return 80
    }

    override fun getHeight(): Int {
        return 8
    }

    companion object {
        val resource = Firmament.identifier("textures/gui/bar.png")
        val left = Texture(resource, 0 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
        val middle = Texture(resource, 4 / 64F, 0 / 64F, 8 / 64F, 8 / 64F)
        val right = Texture(resource, 8 / 64F, 0 / 64F, 12 / 64F, 8 / 64F)
        val segmentOverlay = Texture(resource, 12 / 64F, 0 / 64F, 15 / 64F, 8 / 64F)
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
        if (sectionEnd < progress.get() && width == 4) {
            ScreenDrawing.texturedRect(context, x, y, 4, 8, texture, fillColor.color)
            return
        }
        if (sectionStart > progress.get() && width == 4) {
            ScreenDrawing.texturedRect(context, x, y, 4, 8, texture, emptyColor.color)
            return
        }
        val increasePerPixel = (sectionEnd - sectionStart / 4)
        var valueAtPixel = sectionStart
        for (i in (0 until width)) {
            ScreenDrawing.texturedRect(
                context, x + i, y, 1, 8,
                texture.image, texture.u1 + i / 64F, texture.v1, texture.u1 + (i + 1) / 64F, texture.v2,
                if (valueAtPixel < progress.get()) fillColor.color else emptyColor.color
            )
            valueAtPixel += increasePerPixel
        }
    }

    override fun render(context: GuiImmediateContext) {
        val renderContext = (context.renderContext as ModernRenderContext).drawContext
        var i = 0
        val x = 0
        val y = 0
        while (i < context.width - 4) {
            drawSection(
                renderContext,
                if (i == 0) left else middle,
                x + i, y,
                (context.width - (i + 4)).coerceAtMost(4),
                i * total.get() / context.width, (i + 4) * total.get() / context.width
            )
            i += 4
        }
        drawSection(
            renderContext,
            right,
            x + context.width - 4,
            y,
            4,
            (context.width - 4) * total.get() / context.width,
            total.get()
        )
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)

    }

}

fun Identifier.toMoulConfig(): MyResourceLocation {
    return MyResourceLocation(this.namespace, this.path)
}

fun RenderContext.color(color: Color) {
    color(color.red, color.green, color.blue, color.alpha)
}

fun RenderContext.color(red: Int, green: Int, blue: Int, alpha: Int) {
    color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
}

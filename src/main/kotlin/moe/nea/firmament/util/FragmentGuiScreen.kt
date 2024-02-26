/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class FragmentGuiScreen(
    val dismissOnOutOfBounds: Boolean = true
) : Screen(Text.literal("")) {
    var popup: MoulConfigFragment? = null

    fun createPopup(context: GuiContext, position: Point) {
        popup = MoulConfigFragment(context, position) { popup = null }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.matrices.push()
        context.matrices.translate(0f, 0f, 1000f)
        popup?.render(context, mouseX, mouseY, delta)
        context.matrices.pop()
    }

    private inline fun ifPopup(ifYes: (MoulConfigFragment) -> Unit): Boolean {
        val p = popup ?: return false
        ifYes(p)
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return ifPopup {
            it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return ifPopup {
            it.keyReleased(keyCode, scanCode, modifiers)
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        ifPopup { it.mouseMoved(mouseX, mouseY) }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return ifPopup {
            it.mouseReleased(mouseX, mouseY, button)
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        return ifPopup {
            it.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return ifPopup {
            if (!Rectangle(
                    it.position,
                    Dimension(it.context.root.width, it.context.root.height)
                ).contains(Point(mouseX, mouseY))
                && dismissOnOutOfBounds
            ) {
                popup = null
            } else {
                it.mouseClicked(mouseX, mouseY, button)
            }
        }|| super.mouseClicked(mouseX, mouseY, button)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return ifPopup { it.charTyped(chr, modifiers) }
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        return ifPopup {
            it.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }
    }
}

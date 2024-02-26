/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.buttons

import io.github.notenoughupdates.moulconfig.common.IItemStack
import io.github.notenoughupdates.moulconfig.xml.Bind
import io.github.notenoughupdates.moulconfig.platform.ModernItemStack
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import org.lwjgl.glfw.GLFW
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.FragmentGuiScreen
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils

class InventoryButtonEditor(
    val lastGuiRect: Rectangle,
) : FragmentGuiScreen() {
    inner class Editor(val originalButton: InventoryButton) {
        @field:Bind
        var command: String = originalButton.command ?: ""

        @field:Bind
        var icon: String = originalButton.icon ?: ""

        @Bind
        fun getItemIcon(): IItemStack {
            save()
            return ModernItemStack.of(InventoryButton.getItemForName(icon))
        }

        @Bind
        fun delete() {
            buttons.removeIf { it === originalButton }
            popup = null
        }

        fun save() {
            originalButton.icon = icon
            originalButton.command = command
        }
    }

    var buttons: MutableList<InventoryButton> =
        InventoryButtons.DConfig.data.buttons.map { it.copy() }.toMutableList()

    override fun close() {
        InventoryButtons.DConfig.data.buttons = buttons
        InventoryButtons.DConfig.markDirty()
        super.close()
    }

    override fun init() {
        super.init()
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.load-preset")) {
                val t = ClipboardUtils.getTextContents()
                val newButtons = InventoryButtonTemplates.loadTemplate(t)
                if (newButtons != null)
                    buttons = newButtons.toMutableList()
            }
                .position(lastGuiRect.minX + 10, lastGuiRect.minY + 35)
                .width(lastGuiRect.width - 20)
                .build()
        )
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.save-preset")) {
                ClipboardUtils.setTextContent(InventoryButtonTemplates.saveTemplate(buttons))
            }
                .position(lastGuiRect.minX + 10, lastGuiRect.minY + 60)
                .width(lastGuiRect.width - 20)
                .build()
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(lastGuiRect.minX, lastGuiRect.minY, lastGuiRect.maxX, lastGuiRect.maxY, -1)
        context.setShaderColor(1f, 1f, 1f, 1f)
        super.render(context, mouseX, mouseY, delta)
        for (button in buttons) {
            val buttonPosition = button.getBounds(lastGuiRect)
            context.matrices.push()
            context.matrices.translate(buttonPosition.minX.toFloat(), buttonPosition.minY.toFloat(), 0F)
            button.render(context)
            context.matrices.pop()
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close()
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) return true
        val clickedButton = buttons.firstOrNull { it.getBounds(lastGuiRect).contains(Point(mouseX, mouseY)) }
        if (clickedButton != null) {
            createPopup(MoulConfigUtils.loadGui("button_editor_fragment", Editor(clickedButton)), Point(mouseX, mouseY))
            return true
        }
        if (lastGuiRect.contains(mouseX, mouseY) || lastGuiRect.contains(
                Point(
                    mouseX + InventoryButton.dimensions.width,
                    mouseY + InventoryButton.dimensions.height,
                )
            )
        ) return true
        val mx = mouseX.toInt()
        val my = mouseY.toInt()
        val anchorRight = mx > lastGuiRect.maxX
        val anchorBottom = my > lastGuiRect.maxY
        var offsetX = mx - if (anchorRight) lastGuiRect.maxX else lastGuiRect.minX
        var offsetY = my - if (anchorBottom) lastGuiRect.maxY else lastGuiRect.minY
        if (InputUtil.isKeyPressed(MC.window.handle, InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            offsetX = MathHelper.floor(offsetX / 20F) * 20
            offsetY = MathHelper.floor(offsetY / 20F) * 20
        }
        buttons.add(InventoryButton(offsetX, offsetY, anchorRight, anchorBottom, null, null))
        return true
    }

}

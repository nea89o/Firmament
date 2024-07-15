/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.storageoverlay

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.assertTrueOr

class StorageOverlayScreen : Screen(Text.literal("")) {

    companion object {
        val PLAYER_WIDTH = 184
        val PLAYER_HEIGHT = 91
        val PLAYER_Y_INSET = 3
        val SLOT_SIZE = 18
        val PADDING = 10
        val PAGE_WIDTH = SLOT_SIZE * 9
        val HOTBAR_X = 12
        val HOTBAR_Y = 67
        val MAIN_INVENTORY_Y = 9
        val SCROLL_BAR_WIDTH = 8
        val SCROLL_BAR_HEIGHT = 16
    }

    var isExiting: Boolean = false
    var scroll: Float = 0F
    var pageWidthCount = StorageOverlay.TConfig.columns

    inner class Measurements {
        val innerScrollPanelWidth = PAGE_WIDTH * pageWidthCount + (pageWidthCount - 1) * PADDING
        val overviewWidth = innerScrollPanelWidth + 3 * PADDING + SCROLL_BAR_WIDTH
        val x = width / 2 - overviewWidth / 2
        val overviewHeight = minOf(3 * 18 * 6, height - PLAYER_HEIGHT - minOf(80, height / 10))
        val innerScrollPanelHeight = overviewHeight - PADDING * 2
        val y = height / 2 - (overviewHeight + PLAYER_HEIGHT) / 2
        val playerX = width / 2 - PLAYER_WIDTH / 2
        val playerY = y + overviewHeight - PLAYER_Y_INSET
    }

    var measurements = Measurements()

    var lastRenderedInnerHeight = 0
    override fun init() {
        super.init()
        pageWidthCount = StorageOverlay.TConfig.columns
            .coerceAtMost((width - PADDING) / (PAGE_WIDTH + PADDING))
            .coerceAtLeast(1)
        measurements = Measurements()
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        scroll = (scroll + StorageOverlay.adjustScrollSpeed(verticalAmount)).toFloat()
            .coerceAtMost(getMaxScroll())
            .coerceAtLeast(0F)
        return true
    }

    fun getMaxScroll() = lastRenderedInnerHeight.toFloat() - getScrollPanelInner().height

    val playerInventorySprite = Identifier.of("firmament:storageoverlay/player_inventory")
    val upperBackgroundSprite = Identifier.of("firmament:storageoverlay/upper_background")
    val slotRowSprite = Identifier.of("firmament:storageoverlay/storage_row")
    val scrollbarBackground = Identifier.of("firmament:storageoverlay/scroll_bar_background")
    val scrollbarKnob = Identifier.of("firmament:storageoverlay/scroll_bar_knob")

    override fun close() {
        isExiting = true
        super.close()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        drawBackgrounds(context)
        drawPages(context, mouseX, mouseY, delta, null, null, Point())
        drawScrollBar(context)
        drawPlayerInventory(context, mouseX, mouseY, delta)
    }

    fun getScrollbarPercentage(): Float {
        return scroll / getMaxScroll()
    }

    fun drawScrollBar(context: DrawContext) {
        val sbRect = getScrollBarRect()
        context.drawGuiTexture(
            scrollbarBackground,
            sbRect.minX, sbRect.minY,
            sbRect.width, sbRect.height,
        )
        context.drawGuiTexture(
            scrollbarKnob,
            sbRect.minX, sbRect.minY + (getScrollbarPercentage() * (sbRect.height - SCROLL_BAR_HEIGHT)).toInt(),
            SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT
        )
    }

    fun drawBackgrounds(context: DrawContext) {
        context.drawGuiTexture(upperBackgroundSprite,
                               measurements.x,
                               measurements.y,
                               0,
                               measurements.overviewWidth,
                               measurements.overviewHeight)
        context.drawGuiTexture(playerInventorySprite,
                               measurements.playerX,
                               measurements.playerY,
                               0,
                               PLAYER_WIDTH,
                               PLAYER_HEIGHT)
    }

    fun getPlayerInventorySlotPosition(int: Int): Pair<Int, Int> {
        if (int < 9) {
            return Pair(measurements.playerX + int * SLOT_SIZE + HOTBAR_X, HOTBAR_Y + measurements.playerY)
        }
        return Pair(
            measurements.playerX + (int % 9) * SLOT_SIZE + HOTBAR_X,
            measurements.playerY + (int / 9 - 1) * SLOT_SIZE + MAIN_INVENTORY_Y
        )
    }

    fun drawPlayerInventory(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val items = MC.player?.inventory?.main ?: return
        items.withIndex().forEach { (index, item) ->
            val (x, y) = getPlayerInventorySlotPosition(index)
            context.drawItem(item, x, y, 0)
            context.drawItemInSlot(textRenderer, item, x, y)
        }
    }

    fun getScrollBarRect(): Rectangle {
        return Rectangle(measurements.x + PADDING + measurements.innerScrollPanelWidth + PADDING,
                         measurements.y + PADDING,
                         SCROLL_BAR_WIDTH,
                         measurements.innerScrollPanelHeight)
    }

    fun getScrollPanelInner(): Rectangle {
        return Rectangle(measurements.x + PADDING,
                         measurements.y + PADDING,
                         measurements.innerScrollPanelWidth,
                         measurements.innerScrollPanelHeight)
    }

    fun createScissors(context: DrawContext) {
        val rect = getScrollPanelInner()
        context.enableScissor(
            rect.minX, rect.minY,
            rect.maxX, rect.maxY
        )
    }

    fun drawPages(
        context: DrawContext, mouseX: Int, mouseY: Int, delta: Float,
        excluding: StoragePageSlot?,
        slots: List<Slot>?,
        slotOffset: Point
    ) {
        createScissors(context)
        val data = StorageOverlay.Data.data ?: StorageData()
        layoutedForEach(data) { rect, page, inventory ->
            drawPage(context,
                     rect.x,
                     rect.y,
                     page, inventory,
                     if (excluding == page) slots else null,
                     slotOffset
            )
        }
        context.disableScissor()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return mouseClicked(mouseX, mouseY, button, null)
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int, activePage: StoragePageSlot?): Boolean {
        if (getScrollPanelInner().contains(mouseX, mouseY)) {
            val data = StorageOverlay.Data.data ?: StorageData()
            layoutedForEach(data) { rect, page, _ ->
                if (rect.contains(mouseX, mouseY) && activePage != page && button == 0) {
                    page.navigateTo()
                    return true
                }
            }
            return false
        }
        val sbRect = getScrollBarRect()
        if (sbRect.contains(mouseX, mouseY)) {
            // TODO: support dragging of the mouse and such
            val percentage = (mouseY - sbRect.getY()) / sbRect.getHeight()
            scroll = (getMaxScroll() * percentage).toFloat()
            mouseScrolled(0.0, 0.0, 0.0, 0.0)
            return true
        }
        return false
    }

    private inline fun layoutedForEach(
        data: StorageData,
        func: (
            rectangle: Rectangle,
            page: StoragePageSlot, inventory: StorageData.StorageInventory,
        ) -> Unit
    ) {
        var yOffset = -scroll.toInt()
        var xOffset = 0
        var maxHeight = 0
        for ((page, inventory) in data.storageInventories.entries) {
            val currentHeight = inventory.inventory?.let { it.rows * SLOT_SIZE + 4 + textRenderer.fontHeight }
                ?: 18
            maxHeight = maxOf(maxHeight, currentHeight)
            val rect = Rectangle(
                measurements.x + PADDING + (PAGE_WIDTH + PADDING) * xOffset,
                yOffset + measurements.y + PADDING,
                PAGE_WIDTH,
                currentHeight
            )
            func(rect, page, inventory)
            xOffset++
            if (xOffset >= pageWidthCount) {
                yOffset += maxHeight
                xOffset = 0
                maxHeight = 0
            }
        }
        lastRenderedInnerHeight = maxHeight + yOffset + scroll.toInt()
    }

    fun drawPage(
        context: DrawContext,
        x: Int,
        y: Int,
        page: StoragePageSlot,
        inventory: StorageData.StorageInventory,
        slots: List<Slot>?,
        slotOffset: Point,
    ): Int {
        val inv = inventory.inventory
        if (inv == null) {
            context.drawGuiTexture(upperBackgroundSprite, x, y, PAGE_WIDTH, 18)
            context.drawText(textRenderer,
                             Text.literal("TODO: open this page"),
                             x + 4,
                             y + 4,
                             -1,
                             true)
            return 18
        }
        assertTrueOr(slots == null || slots.size == inv.stacks.size) { return 0 }
        val name = page.defaultName()
        context.drawText(textRenderer, Text.literal(name), x + 4, y + 2,
                         if (slots == null) 0xFFFFFFFF.toInt() else 0xFFFFFF00.toInt(), true)
        context.drawGuiTexture(slotRowSprite, x, y + 4 + textRenderer.fontHeight, PAGE_WIDTH, inv.rows * SLOT_SIZE)
        inv.stacks.forEachIndexed { index, stack ->
            val slotX = (index % 9) * SLOT_SIZE + x + 1
            val slotY = (index / 9) * SLOT_SIZE + y + 4 + textRenderer.fontHeight + 1
            if (slots == null) {
                context.drawItem(stack, slotX, slotY)
                context.drawItemInSlot(textRenderer, stack, slotX, slotY)
            } else {
                val slot = slots[index]
                slot.x = slotX - slotOffset.x
                slot.y = slotY - slotOffset.y
            }
        }
        return inv.rows * SLOT_SIZE + 4 + textRenderer.fontHeight
    }

    fun getBounds(): List<Rectangle> {
        return listOf(
            Rectangle(measurements.x,
                      measurements.y,
                      measurements.overviewWidth,
                      measurements.overviewHeight),
            Rectangle(measurements.playerX,
                      measurements.playerY,
                      PLAYER_WIDTH,
                      PLAYER_HEIGHT))
    }
}

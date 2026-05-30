

package moe.nea.firmament.features.inventory.storageoverlay

import org.lwjgl.glfw.GLFW
import kotlin.math.max
import net.minecraft.world.level.block.Blocks
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.toShedaniel

class StorageOverviewScreen() : Screen(Component.empty()) {
    companion object {
        val emptyStorageSlotItems = listOf<Item>(
            Blocks.RED_STAINED_GLASS_PANE.asItem(),
            Blocks.BROWN_STAINED_GLASS_PANE.asItem(),
            Items.GRAY_DYE
        )
        val pageWidth get() = 19 * 9

		var scroll = 0
		var lastRenderedHeight = 0
    }

    val content = StorageOverlay.Data.data ?: StorageData()
    var isClosing = false

	override fun init() {
		super.init()
		scroll = scroll.coerceAtMost(getMaxScroll()).coerceAtLeast(0)
	}

	override fun onClose() {
		if (!StorageOverlay.TConfig.retainScroll) scroll = 0
		super.onClose()
	}

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.fill(0, 0, width, height, 0x90000000.toInt())
        layoutedForEach { (key, value), offsetX, offsetY ->
            context.pose().pushMatrix()
            context.pose().translate(offsetX.toFloat(), offsetY.toFloat())
            renderStoragePage(context, value, mouseX - offsetX, mouseY - offsetY)
            context.pose().popMatrix()
        }
    }

    inline fun layoutedForEach(onEach: (data: Pair<StoragePageSlot, StorageData.StorageInventory>, offsetX: Int, offsetY: Int) -> Unit) {
        var offsetY = 0
        var currentMaxHeight = StorageOverlay.TConfig.margin - StorageOverlay.TConfig.padding - scroll
        var totalHeight = -currentMaxHeight
        content.storageInventories.onEachIndexed { index, (key, value) ->
            val pageX = (index % StorageOverlay.TConfig.columns)
            if (pageX == 0) {
                currentMaxHeight += StorageOverlay.TConfig.padding
                offsetY += currentMaxHeight
                totalHeight += currentMaxHeight
                currentMaxHeight = 0
            }
            val xPosition =
                width / 2 - (StorageOverlay.TConfig.columns * (pageWidth + StorageOverlay.TConfig.padding) - StorageOverlay.TConfig.padding) / 2 + pageX * (pageWidth + StorageOverlay.TConfig.padding)
            onEach(Pair(key, value), xPosition, offsetY)
            val height = getStorePageHeight(value)
            currentMaxHeight = max(currentMaxHeight, height)
        }
        lastRenderedHeight = totalHeight + currentMaxHeight
    }

	override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        layoutedForEach { (k, p), x, y ->
            val rx = click.x - x
            val ry = click.y - y
            if (rx in (0.0..pageWidth.toDouble()) && ry in (0.0..getStorePageHeight(p).toDouble())) {
                onClose()
                StorageOverlay.lastStorageOverlay = this
                k.navigateTo()
                return true
            }
        }
        return super.mouseClicked(click, doubled)
    }

    fun getStorePageHeight(page: StorageData.StorageInventory): Int {
        return page.inventory?.rows?.let { it * 19 + MC.font.lineHeight + 2 } ?: 60
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        scroll =
            (scroll + StorageOverlay.adjustScrollSpeed(verticalAmount)).toInt()
                .coerceAtMost(getMaxScroll()).coerceAtLeast(0)
        return true
    }

	private fun getMaxScroll() = lastRenderedHeight - height + 2 * StorageOverlay.TConfig.margin

    private fun renderStoragePage(context: GuiGraphics, page: StorageData.StorageInventory, mouseX: Int, mouseY: Int) {
        context.drawString(MC.font, content.displayName(page.slot), 2, 2, -1, true)
        val inventory = page.inventory
        if (inventory == null) {
            // TODO: Missing texture
            context.fill(0, 0, pageWidth, 60, DyeColor.RED.toShedaniel().darker(4.0).color)
            context.drawCenteredString(MC.font, Component.literal("Not loaded yet"), pageWidth / 2, 30, -1)
            return
        }

        for ((index, stack) in inventory.stacks.withIndex()) {
            val x = (index % 9) * 19
            val y = (index / 9) * 19 + MC.font.lineHeight + 2
            if (((mouseX - x) in 0 until 18) && ((mouseY - y) in 0 until 18)) {
                context.fill(x, y, x + 18, y + 18, 0x80808080.toInt())
            } else {
                context.fill(x, y, x + 18, y + 18, 0x40808080.toInt())
            }
            context.renderItem(stack, x + 1, y + 1)
            context.renderItemDecorations(MC.font, stack, x + 1, y + 1)
        }
    }

	override fun keyPressed(input: KeyEvent): Boolean {
        if (input.input() == GLFW.GLFW_KEY_ESCAPE)
            isClosing = true
        return super.keyPressed(input)
    }
}

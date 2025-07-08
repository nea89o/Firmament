package moe.nea.firmament.features.inventory.storageoverlay

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.ColumnComponent
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.observer.Property
import java.util.TreeSet
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.gui.EmptyComponent
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils.adopt
import moe.nea.firmament.util.MoulConfigUtils.clickMCComponentInPlace
import moe.nea.firmament.util.MoulConfigUtils.drawMCComponentInPlace
import moe.nea.firmament.util.MoulConfigUtils.typeMCComponentInPlace
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.assertTrueOr
import moe.nea.firmament.util.customgui.customGui
import moe.nea.firmament.util.mc.FakeSlot
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.render.enableScissorWithoutTranslation
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString

class StorageOverlayScreen : Screen(Text.literal("")) {

	companion object {
		val PLAYER_WIDTH = 184
		val PLAYER_HEIGHT = 91
		val PLAYER_Y_INSET = 3
		val SLOT_SIZE = 18
		val PADDING = 10
		val PAGE_SLOTS_WIDTH = SLOT_SIZE * 9
		val PAGE_WIDTH = PAGE_SLOTS_WIDTH + 4
		val HOTBAR_X = 12
		val HOTBAR_Y = 67
		val MAIN_INVENTORY_Y = 9
		val SCROLL_BAR_WIDTH = 8
		val SCROLL_BAR_HEIGHT = 16
		val CONTROL_X_INSET = 3
		val CONTROL_Y_INSET = 5
		val CONTROL_WIDTH = 70
		val CONTROL_BACKGROUND_WIDTH = CONTROL_WIDTH + CONTROL_X_INSET + 1
		val CONTROL_HEIGHT = 50

		var scroll: Float = 0F
		var lastRenderedInnerHeight = 0

		fun resetScroll() {
			if (!StorageOverlay.TConfig.retainScroll) scroll = 0F
		}
	}

	var isExiting: Boolean = false
	var pageWidthCount = StorageOverlay.TConfig.columns

	inner class Measurements {
		val innerScrollPanelWidth = PAGE_WIDTH * pageWidthCount + (pageWidthCount - 1) * PADDING
		val overviewWidth = innerScrollPanelWidth + 3 * PADDING + SCROLL_BAR_WIDTH
		val x = width / 2 - overviewWidth / 2
		val overviewHeight = minOf(
			height - PLAYER_HEIGHT - minOf(80, height / 10),
			StorageOverlay.TConfig.height
		)
		val innerScrollPanelHeight = overviewHeight - PADDING * 2
		val y = height / 2 - (overviewHeight + PLAYER_HEIGHT) / 2
		val playerX = width / 2 - PLAYER_WIDTH / 2
		val playerY = y + overviewHeight - PLAYER_Y_INSET
		val controlX = playerX - CONTROL_WIDTH + CONTROL_X_INSET
		val controlY = playerY - CONTROL_Y_INSET
		val totalWidth = overviewWidth
		val totalHeight = overviewHeight - PLAYER_Y_INSET + PLAYER_HEIGHT
	}

	var measurements = Measurements()

	public override fun init() {
		super.init()
		pageWidthCount = StorageOverlay.TConfig.columns
			.coerceAtMost((width - PADDING) / (PAGE_WIDTH + PADDING))
			.coerceAtLeast(1)
		measurements = Measurements()
		scroll = scroll.coerceAtMost(getMaxScroll()).coerceAtLeast(0F)
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		coerceScroll(StorageOverlay.adjustScrollSpeed(verticalAmount).toFloat())
		return true
	}

	fun coerceScroll(offset: Float) {
		scroll = (scroll + offset)
			.coerceAtMost(getMaxScroll())
			.coerceAtLeast(0F)
	}

	fun getMaxScroll() = lastRenderedInnerHeight.toFloat() - getScrollPanelInner().height

	val playerInventorySprite = Identifier.of("firmament:storageoverlay/player_inventory")
	val upperBackgroundSprite = Identifier.of("firmament:storageoverlay/upper_background")
	val slotRowSprite = Identifier.of("firmament:storageoverlay/storage_row")
	val scrollbarBackground = Identifier.of("firmament:storageoverlay/scroll_bar_background")
	val scrollbarKnob = Identifier.of("firmament:storageoverlay/scroll_bar_knob")
	val controllerBackground = Identifier.of("firmament:storageoverlay/storage_controls")

	override fun close() {
		isExiting = true
		resetScroll()
		super.close()
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		drawBackgrounds(context)
		drawPages(context, mouseX, mouseY, delta, null, null, Point())
		drawScrollBar(context)
		drawPlayerInventory(context, mouseX, mouseY, delta)
		drawControls(context, mouseX, mouseY)
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

	fun editPages() {
		isExiting = true
		MC.instance.send {
			val hs = MC.screen as? HandledScreen<*>
			if (StorageBackingHandle.fromScreen(hs) is StorageBackingHandle.Overview) {
				hs.customGui = null
				hs.init(MC.instance, width, height)
			} else {
				MC.sendCommand("storage")
			}
		}
	}

	val guiContext = GuiContext(EmptyComponent())
	private val knobStub = EmptyComponent()
	val editButton = FirmButtonComponent(
		TextComponent(tr("firmament.storage-overlay.edit-pages", "Edit Pages").string),
		action = ::editPages
	)
	val searchText = Property.of("") // TODO: sync with REI
	val searchField = TextFieldComponent(
		searchText, 100, GetSetter.constant(true),
		tr("firmament.storage-overlay.search.suggestion", "Search...").string,
		IMinecraft.instance.defaultFontRenderer
	)
	val controlComponent = PanelComponent(
		ColumnComponent(
			searchField,
			editButton,
		),
		8, PanelComponent.DefaultBackgroundRenderer.TRANSPARENT
	)

	init {
		searchText.addObserver { _, _ ->
			layoutedForEach(StorageOverlay.Data.data ?: StorageData(), { _, _, _ -> })
			coerceScroll(0F)
		}
		guiContext.adopt(knobStub)
		guiContext.adopt(controlComponent)
	}

	fun drawControls(context: DrawContext, mouseX: Int, mouseY: Int) {
		context.drawGuiTexture(
			controllerBackground,
			measurements.controlX,
			measurements.controlY,
			CONTROL_BACKGROUND_WIDTH, CONTROL_HEIGHT
		)
		context.drawMCComponentInPlace(
			controlComponent,
			measurements.controlX, measurements.controlY,
			CONTROL_WIDTH, CONTROL_HEIGHT,
			mouseX, mouseY
		)
	}

	fun drawBackgrounds(context: DrawContext) {
		context.drawGuiTexture(
			upperBackgroundSprite,
			measurements.x,
			measurements.y,
			measurements.overviewWidth,
			measurements.overviewHeight
		)
		context.drawGuiTexture(
			playerInventorySprite,
			measurements.playerX,
			measurements.playerY,
			PLAYER_WIDTH,
			PLAYER_HEIGHT
		)
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
		val items = MC.player?.inventory?.mainStacks ?: return
		items.withIndex().forEach { (index, item) ->
			val (x, y) = getPlayerInventorySlotPosition(index)
			context.drawItem(item, x, y, 0)
			context.drawStackOverlay(textRenderer, item, x, y)
		}
	}

	fun getScrollBarRect(): Rectangle {
		return Rectangle(
			measurements.x + PADDING + measurements.innerScrollPanelWidth + PADDING,
			measurements.y + PADDING,
			SCROLL_BAR_WIDTH,
			measurements.innerScrollPanelHeight
		)
	}

	fun getScrollPanelInner(): Rectangle {
		return Rectangle(
			measurements.x + PADDING,
			measurements.y + PADDING,
			measurements.innerScrollPanelWidth,
			measurements.innerScrollPanelHeight
		)
	}

	fun createScissors(context: DrawContext) {
		val rect = getScrollPanelInner()
		context.enableScissorWithoutTranslation(
			rect.minX.toFloat(), rect.minY.toFloat(),
			rect.maxX.toFloat(), rect.maxY.toFloat(),
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
			drawPage(
				context,
				rect.x,
				rect.y,
				page, inventory,
				if (excluding == page) slots else null,
				slotOffset
			)
		}
		context.disableScissor()
	}


	var knobGrabbed: Boolean
		get() = guiContext.focusedElement == knobStub
		set(value) = knobStub.setFocus(value)

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return mouseClicked(mouseX, mouseY, button, null)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (knobGrabbed) {
			knobGrabbed = false
			return true
		}
		if (clickMCComponentInPlace(
				controlComponent,
				measurements.controlX, measurements.controlY,
				CONTROL_WIDTH, CONTROL_HEIGHT,
				mouseX.toInt(), mouseY.toInt(),
				MouseEvent.Click(button, false)
			)
		) return true
		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (knobGrabbed) {
			val sbRect = getScrollBarRect()
			val percentage = (mouseY - sbRect.getY()) / sbRect.getHeight()
			scroll = (getMaxScroll() * percentage).toFloat()
			mouseScrolled(0.0, 0.0, 0.0, 0.0)
			return true
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	fun mouseClicked(mouseX: Double, mouseY: Double, button: Int, activePage: StoragePageSlot?): Boolean {
		guiContext.setFocusedElement(null) // Blur all elements. They will be refocused by clickMCComponentInPlace if in doubt, and we don't have any double click components.
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
			val percentage = (mouseY - sbRect.getY()) / sbRect.getHeight()
			scroll = (getMaxScroll() * percentage).toFloat()
			mouseScrolled(0.0, 0.0, 0.0, 0.0)
			knobGrabbed = true
			return true
		}
		if (clickMCComponentInPlace(
				controlComponent,
				measurements.controlX, measurements.controlY,
				CONTROL_WIDTH, CONTROL_HEIGHT,
				mouseX.toInt(), mouseY.toInt(),
				MouseEvent.Click(button, true)
			)
		) return true
		return false
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		if (typeMCComponentInPlace(
				controlComponent,
				measurements.controlX, measurements.controlY,
				CONTROL_WIDTH, CONTROL_HEIGHT,
				KeyboardEvent.CharTyped(chr)
			)
		) {
			return true
		}
		return super.charTyped(chr, modifiers)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (typeMCComponentInPlace(
				controlComponent,
				measurements.controlX, measurements.controlY,
				CONTROL_WIDTH, CONTROL_HEIGHT,
				KeyboardEvent.KeyPressed(keyCode, false)
			)
		) {
			return true
		}
		return super.keyReleased(keyCode, scanCode, modifiers)
	}

	override fun shouldCloseOnEsc(): Boolean {
		return this === MC.screen // Fixes this UI closing the handled screen on Escape press.
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (typeMCComponentInPlace(
				controlComponent,
				measurements.controlX, measurements.controlY,
				CONTROL_WIDTH, CONTROL_HEIGHT,
				KeyboardEvent.KeyPressed(keyCode, true)
			)
		) {
			return true
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}


	var searchCache: String? = null
	var filteredPagesCache = setOf<StoragePageSlot>()

	fun getFilteredPages(): Set<StoragePageSlot> {
		val searchValue = searchText.get()
		val data = StorageOverlay.Data.data ?: return filteredPagesCache // Do not update cache if data is missing
		if (searchCache == searchValue) return filteredPagesCache
		val result =
			data.storageInventories
				.entries.asSequence()
				.filter { it.value.inventory?.stacks?.any { matchesSearch(it, searchValue) } ?: true }
				.map { it.key }
				.toSet()
		searchCache = searchValue
		filteredPagesCache = result
		return result
	}


	fun matchesSearch(itemStack: ItemStack, search: String): Boolean {
		val searchWords = search.words().toCollection(TreeSet())
		fun removePrefixes(value: String) {
			searchWords.removeIf { value.contains(it, ignoreCase = true) }
		}
		itemStack.displayNameAccordingToNbt.unformattedString.words().forEach(::removePrefixes)
		if (searchWords.isEmpty()) return true
		itemStack.loreAccordingToNbt.forEach {
			it.unformattedString.words().forEach(::removePrefixes)
		}
		return searchWords.isEmpty()
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
		val filter = getFilteredPages()
		for ((page, inventory) in data.storageInventories.entries) {
			if (page !in filter) continue
			val currentHeight = inventory.inventory?.let { it.rows * SLOT_SIZE + 6 + textRenderer.fontHeight }
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
			context.drawText(
				textRenderer,
				Text.literal("TODO: open this page"),
				x + 4,
				y + 4,
				-1,
				true
			)
			return 18
		}
		assertTrueOr(slots == null || slots.size == inv.stacks.size) { return 0 }
		val name = page.defaultName()
		val pageHeight = inv.rows * SLOT_SIZE + 8 + textRenderer.fontHeight
		if (slots != null && StorageOverlay.TConfig.outlineActiveStoragePage)
			context.drawBorder(
				x,
				y + 3 + textRenderer.fontHeight,
				PAGE_WIDTH,
				inv.rows * SLOT_SIZE + 4,
				StorageOverlay.TConfig.outlineActiveStoragePageColour.getEffectiveColourRGB()
			)
		context.drawText(
			textRenderer, Text.literal(name), x + 6, y + 3,
			if (slots == null) 0xFFFFFFFF.toInt() else 0xFFFFFF00.toInt(), true
		)
		context.drawGuiTexture(
			slotRowSprite,
			x + 2,
			y + 5 + textRenderer.fontHeight,
			PAGE_SLOTS_WIDTH,
			inv.rows * SLOT_SIZE
		)
		inv.stacks.forEachIndexed { index, stack ->
			val slotX = (index % 9) * SLOT_SIZE + x + 3
			val slotY = (index / 9) * SLOT_SIZE + y + 5 + textRenderer.fontHeight + 1
			val fakeSlot = FakeSlot(stack, slotX, slotY)
			if (slots == null) {
				SlotRenderEvents.Before.publish(SlotRenderEvents.Before(context, fakeSlot))
				context.drawItem(stack, slotX, slotY)
				context.drawStackOverlay(textRenderer, stack, slotX, slotY)
				SlotRenderEvents.After.publish(SlotRenderEvents.After(context, fakeSlot))
			} else {
				val slot = slots[index]
				slot.x = slotX - slotOffset.x
				slot.y = slotY - slotOffset.y
			}
		}
		return pageHeight + 6
	}

	fun getBounds(): List<Rectangle> {
		return listOf(
			Rectangle(
				measurements.x,
				measurements.y,
				measurements.overviewWidth,
				measurements.overviewHeight
			),
			Rectangle(
				measurements.playerX,
				measurements.playerY,
				PLAYER_WIDTH,
				PLAYER_HEIGHT
			),
			Rectangle(
				measurements.controlX,
				measurements.controlY,
				CONTROL_WIDTH,
				CONTROL_HEIGHT
			)
		)
	}
}

package moe.nea.firmament.features.items.recipes

import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.observer.Property
import java.util.Optional
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.navigation.ScreenAxis
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.api.v1.FirmamentAPI
import moe.nea.firmament.events.HandledScreenClickEvent
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.ReloadRegistrationEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.accessors.castAccessor
import moe.nea.firmament.util.render.drawAlignedBox
import moe.nea.firmament.util.render.drawLine
import moe.nea.firmament.util.skyblockId

object ItemList {
	// TODO: add a global toggle for this and RecipeRegistry

	fun collectExclusions(screen: Screen): Set<ScreenRectangle> {
		val exclusions = mutableSetOf<ScreenRectangle>()
		if (screen is AbstractContainerScreen<*>) {
			val screenHandler = screen.castAccessor()
			exclusions.add(
				ScreenRectangle(
					screenHandler.x_Firmament,
					screenHandler.y_Firmament,
					screenHandler.backgroundWidth_Firmament,
					screenHandler.backgroundHeight_Firmament
				)
			)
		}
		FirmamentAPI.getInstance().extensions
			.forEach { extension ->
				for (rectangle in extension.getExclusionZones(screen)) {
					if (exclusions.any { it.encompasses(rectangle) })
						continue
					exclusions.add(rectangle)
				}
			}

		return exclusions
	}

	var reachableItems = listOf<SBItemStack>()
	var pageOffset = 0
	fun recalculateVisibleItems() {
		reachableItems = RepoManager.neuRepo.items
			.items.values.map { SBItemStack(it.skyblockId) }
	}

	@Subscribe
	fun onReload(event: ReloadRegistrationEvent) {
		event.repo.registerReloadListener { recalculateVisibleItems() }
	}

	fun coordinates(outer: ScreenRectangle, exclusions: Collection<ScreenRectangle>): Sequence<ScreenRectangle> {
		val entryWidth = 18
		val columns = outer.width / entryWidth
		val rows = outer.height / entryWidth
		val lowX = outer.right() - columns * entryWidth
		val lowY = outer.top()
		return generateSequence(0) { it + 1 }
			.map {
				val xIndex = it % columns
				val yIndex = it / columns
				ScreenRectangle(
					lowX + xIndex * entryWidth, lowY + yIndex * entryWidth,
					entryWidth, entryWidth
				)
			}
			.take(rows * columns)
			.filter { candidate -> exclusions.none { it.intersects(candidate) } }
	}

	var lastRenderPositions: List<Pair<ScreenRectangle, SBItemStack>> = listOf()
	var lastHoveredItemStack: Pair<ScreenRectangle, SBItemStack>? = null

	abstract class ItemListElement(
	) : Renderable, GuiEventListener {
		abstract val rectangle: Rectangle
		override fun setFocused(focused: Boolean) {
		}

		override fun isFocused(): Boolean {
			return false
		}

		override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
			return rectangle.contains(mouseX, mouseY)
		}
	}

	interface HasLabel {
		fun component(): Component
	}


	class PopupSettingsElement<T : HasLabel>(
		x: Int,
		y: Int,
		width: Int,
		val selected: GetSetter<T>,
		val options: List<T>,
	) : ItemListElement() {
		override val rectangle: Rectangle = Rectangle(x, y, width, 4 + (MC.font.lineHeight + 2) * options.size)
		fun bb(i: Int) =
			Rectangle(
				rectangle.minX, rectangle.minY + (2 + MC.font.lineHeight) * i + 2,
				rectangle.width, MC.font.lineHeight
			)

		override fun render(
			guiGraphics: GuiGraphics,
			mouseX: Int,
			mouseY: Int,
			partialTick: Float
		) {
			guiGraphics.fill(rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, 0xFF000000.toInt())
			guiGraphics.drawAlignedBox(rectangle.x, rectangle.y, rectangle.width, rectangle.height, -1)
			val sel = selected.get()
			for ((index, element) in options.withIndex()) {
				val b = bb(index)
				val tw = MC.font.width(element.component())
				guiGraphics.drawString(
					MC.font, element.component(), b.centerX - tw / 2,
					b.y + 1,
					if (element == sel) 0xFFA0B000.toInt() else -1
				)
				if (b.contains(mouseX, mouseY))
					guiGraphics.hLine(b.centerX - tw / 2, b.centerX + tw / 2 - 1, b.maxY + 1, -1)
			}
		}

		override fun mouseClicked(event: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
			popupElement = null
			for ((index, element) in options.withIndex()) {
				val b = bb(index)
				if (b.contains(event.x, event.y)) {
					selected.set(element)
					break
				}
			}
			return true
		}
	}

	class SettingElement<T : HasLabel>(
		x: Int,
		y: Int,
		val selected: GetSetter<T>,
		val options: List<T>
	) : ItemListElement() {
		val height = MC.font.lineHeight + 4
		val width = options.maxOf { MC.font.width(it.component()) } + 4
		override val rectangle: Rectangle = Rectangle(x, y, width, height)

		override fun render(
			guiGraphics: GuiGraphics,
			mouseX: Int,
			mouseY: Int,
			partialTick: Float
		) {
			guiGraphics.drawCenteredString(MC.font, selected.get().component(), rectangle.centerX, rectangle.y + 2, -1)
			if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
				guiGraphics.hLine(rectangle.minX, rectangle.maxX - 1, rectangle.maxY - 2, -1)
			}
		}

		override fun mouseClicked(
			event: MouseButtonEvent,
			isDoubleClick: Boolean
		): Boolean {
			popupElement = PopupSettingsElement(
				rectangle.x,
				rectangle.y - options.size * (MC.font.lineHeight + 2) - 2,
				width,
				selected,
				options
			)
			return true
		}
	}

	var popupElement: ItemListElement? = null


	fun findStackUnder(mouseX: Int, mouseY: Int): Pair<ScreenRectangle, SBItemStack>? {
		val lhis = lastHoveredItemStack
		if (lhis != null && lhis.first.containsPoint(mouseX, mouseY))
			return lhis
		return lastRenderPositions.firstOrNull { it.first.containsPoint(mouseX, mouseY) }
	}

	val isItemListEnabled get() = false

	@Subscribe
	fun onClick(event: HandledScreenClickEvent) {
		if(!isItemListEnabled)return
		val pe = popupElement
		val me = MouseButtonEvent(
			event.mouseX, event.mouseY,
			MouseButtonInfo(event.button, 0) // TODO: missing modifiers
		)
		if (pe != null) {
			event.cancel()
			if (!pe.isMouseOver(event.mouseX, event.mouseY)) {
				popupElement = null
				return
			}
			pe.mouseClicked(
				me,
				false
			)
			return
		}
		listElements.forEach {
			if (it.isMouseOver(event.mouseX, event.mouseY))
				it.mouseClicked(me, false)
		}
	}

	var listElements = listOf<ItemListElement>()

	@Subscribe
	fun onRender(event: HandledScreenForegroundEvent) {
		if(!isItemListEnabled) return
		lastHoveredItemStack = null
		lastRenderPositions = listOf()
		val exclusions = collectExclusions(event.screen)
		val potentiallyVisible = reachableItems.subList(pageOffset, reachableItems.size)
		val screenWidth = event.screen.width
		val rightThird = ScreenRectangle(
			screenWidth - screenWidth / 3, 0,
			screenWidth / 3, event.screen.height - MC.font.lineHeight - 4
		)
		val coords = coordinates(rightThird, exclusions)

		lastRenderPositions = coords.zip(potentiallyVisible.asSequence()).toList()
		val isPopupHovered = popupElement?.isMouseOver(event.mouseX.toDouble(),event.mouseY.toDouble())
			?: false
		lastRenderPositions.forEach { (pos, stack) ->
			val realStack = stack.asLazyImmutableItemStack()
			val toRender = realStack ?: ItemStack(Items.PAINTING)
			event.context.renderItem(toRender, pos.left() + 1, pos.top() + 1)
			if (!isPopupHovered && pos.containsPoint(event.mouseX, event.mouseY)) {
				lastHoveredItemStack = pos to stack
				event.context.setTooltipForNextFrame(
					MC.font,
					if (realStack != null)
						ItemSlotWidget.getTooltip(realStack)
					else
						stack.estimateLore(),
					Optional.empty(),
					event.mouseX, event.mouseY
				)
			}
		}
		event.context.fill(
			rightThird.left(),
			rightThird.bottom(),
			rightThird.right(),
			event.screen.height,
			0xFF000000.toInt()
		)
		val le = mutableListOf<ItemListElement>()
		le.add(
			SettingElement(
				0,
				rightThird.bottom(),
				sortOrder,
				SortOrder.entries
			)
		)
		val bottomWidth = le.sumOf { it.rectangle.width + 2 } - 2
		var startX = rightThird.getCenterInAxis(ScreenAxis.HORIZONTAL) - bottomWidth / 2
		le.forEach {
			it.rectangle.translate(startX, 0)
			startX += it.rectangle.width + 2
		}
		le.forEach { it.render(event.context, event.mouseX, event.mouseY, event.delta) }
		listElements = le
		popupElement?.render(event.context, event.mouseX, event.mouseY, event.delta)
	}

	enum class SortOrder(val component: Component) : HasLabel {
		NAME(Component.literal("Name")),
		RARITY(Component.literal("Rarity"));

		override fun component(): Component = component
	}

	val sortOrder = Property.of(SortOrder.NAME)
}

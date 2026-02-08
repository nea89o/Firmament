package moe.nea.firmament.repo.recipes

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import moe.nea.firmament.repo.SBItemStack

interface RecipeLayouter {
	enum class SlotKind {
		SMALL_INPUT,
		SMALL_OUTPUT,

		/**
		 * Create a bigger background and mark the slot as output. The coordinates should still refer the upper left corner of the item stack, not of the bigger background.
		 */
		BIG_OUTPUT,
		DISPLAY,;
		val isBig get() = this == BIG_OUTPUT
	}


	fun createCyclingItemSlot(
		x: Int, y: Int,
		content: List<SBItemStack>,
		slotKind: SlotKind
	): CyclingItemSlot

	fun createItemSlot(
		x: Int, y: Int,
		content: SBItemStack?,
		slotKind: SlotKind,
	): ItemSlot = createCyclingItemSlot(x, y, listOfNotNull(content), slotKind)

	interface CyclingItemSlot : ItemSlot {
		fun onUpdate(action: () -> Unit)
	}

	interface ItemSlot : Updater<SBItemStack> {
		fun current(): SBItemStack
	}

	interface Updater<T> {
		fun update(newValue: T)
	}

	fun createTooltip(rectangle: Rectangle, label: List<Component>)
	fun createTooltip(rectangle: Rectangle, vararg label: Component) =
		createTooltip(rectangle, label.toList())


	fun createLabel(
		x: Int, y: Int,
		text: Component
	): Updater<Component>

	fun createArrow(x: Int, y: Int): Rectangle

	fun createMoulConfig(x: Int, y: Int, w: Int, h: Int, component: GuiComponent)
	fun createFire(point: Point, animationTicks: Int)
	fun createEntity(rectangle: Rectangle, entity: LivingEntity)
}


package moe.nea.firmament.compat.rei

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import net.minecraft.text.Text
import moe.nea.firmament.compat.rei.recipes.wrapWidget
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.repo.recipes.RecipeLayouter

class REIRecipeLayouter : RecipeLayouter {
	val container: MutableList<Widget> = mutableListOf()
	override fun createItemSlot(
		x: Int,
		y: Int,
		content: SBItemStack?,
		slotKind: RecipeLayouter.SlotKind
	) {
		val slot = Widgets.createSlot(Point(x, y))
		if (content != null)
			slot.entry(SBItemEntryDefinition.getEntry(content))
		when (slotKind) {
			RecipeLayouter.SlotKind.SMALL_INPUT -> slot.markInput()
			RecipeLayouter.SlotKind.SMALL_OUTPUT -> slot.markOutput()
			RecipeLayouter.SlotKind.BIG_OUTPUT -> {
				slot.markOutput().disableBackground()
				container.add(Widgets.createResultSlotBackground(Point(x, y)))
			}
		}
		container.add(slot)
	}

	override fun createLabel(x: Int, y: Int, text: Text) {
		container.add(Widgets.createLabel(Point(x, y), text))
	}

	override fun createArrow(x: Int, y: Int) {
		container.add(Widgets.createArrow(Point(x, y)))
	}

	override fun createMoulConfig(
		x: Int,
		y: Int,
		w: Int,
		h: Int,
		component: GuiComponent
	) {
		container.add(wrapWidget(Rectangle(Point(x, y), Dimension(w, h)), component))
	}
}

package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.ScreenRectangle
import moe.nea.firmament.util.mc.asScreenRectangle

abstract class RecipeWidget : GuiEventListener, Renderable, NarratableEntry {
	override fun narrationPriority(): NarratableEntry.NarrationPriority {
		return NarratableEntry.NarrationPriority.NONE// I am so sorry
	}

	override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
	}

	open fun tick() {}
	private var _focused = false
	abstract var position: Point
	abstract val size: Dimension
	open val rect: Rectangle get() = Rectangle(position, size)
	override fun setFocused(focused: Boolean) {
		this._focused = focused
	}

	override fun isFocused(): Boolean {
		return this._focused
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return rect.contains(mouseX, mouseY)
	}
}

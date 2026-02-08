package moe.nea.firmament.features.items.recipes

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

class ArrowWidget(override var position: Point) : RecipeWidget() {
	override val size: Dimension
		get() = Dimension(14, 14)

	companion object {
		val arrowSprite = Identifier.withDefaultNamespace("container/furnace/lit_progress")
	}

	override fun render(
		guiGraphics: GuiGraphics,
		mouseX: Int,
		mouseY: Int,
		partialTick: Float
	) {
		guiGraphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			arrowSprite,
			14,
			14,
			0,
			0,
			position.x,
			position.y,
			14,
			14
		)
	}

}

package moe.nea.firmament.compat.rei

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.entity.LivingEntity
import moe.nea.firmament.gui.entity.EntityRenderer
import moe.nea.firmament.util.ErrorUtil


class EntityWidget(val entity: LivingEntity?, val point: Point) : WidgetWithBounds() {
	override fun children(): List<Element> {
		return emptyList()
	}

	var hasErrored = false

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		try {
			if (!hasErrored)
				EntityRenderer.renderEntity(entity!!, context, point.x, point.y, mouseX.toFloat(), mouseY.toFloat())
		} catch (ex: Exception) {
			ErrorUtil.softError("Failed to render constructed entity: $entity", ex)
			hasErrored = true
		}
		if (hasErrored) {
			context.fill(point.x, point.y, point.x + 50, point.y + 80, 0xFFAA2222.toInt())
		}
	}

	override fun getBounds(): Rectangle {
		return Rectangle(point, Dimension(50, 80))
	}
}

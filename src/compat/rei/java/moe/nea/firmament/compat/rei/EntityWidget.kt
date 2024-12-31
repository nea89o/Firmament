package moe.nea.firmament.compat.rei

import me.shedaniel.math.Dimension
import me.shedaniel.math.FloatingDimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.entity.LivingEntity
import moe.nea.firmament.gui.entity.EntityRenderer
import moe.nea.firmament.util.ErrorUtil


class EntityWidget(
	val entity: LivingEntity?,
	val point: Point,
	val size: FloatingDimension = FloatingDimension(defaultSize)
) : WidgetWithBounds() {
	override fun children(): List<Element> {
		return emptyList()
	}

	var hasErrored = false

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		try {
			context.matrices.push()
			if (!hasErrored) {
				context.matrices.translate(point.x.toDouble(), point.y.toDouble(), 0.0)
				val xScale = size.width / defaultSize.width.toDouble()
				val yScale = size.height / defaultSize.height.toDouble()
				context.matrices.scale(xScale.toFloat(), yScale.toFloat(), 1.0F)
				EntityRenderer.renderEntity(
					entity!!,
					context,
					0, 0,
					(mouseX - point.x) * xScale,
					(mouseY - point.y) * yScale)
			}
		} catch (ex: Exception) {
			ErrorUtil.softError("Failed to render constructed entity: $entity", ex)
			hasErrored = true
		} finally {
			context.matrices.pop()
		}
		if (hasErrored) {
			context.fill(point.x, point.y, point.x + size.width.toInt(), point.y + size.height.toInt(), 0xFFAA2222.toInt())
		}
	}

	companion object {
		val defaultSize = Dimension(50, 80)
	}

	override fun getBounds(): Rectangle {
		return Rectangle(point, size)
	}
}

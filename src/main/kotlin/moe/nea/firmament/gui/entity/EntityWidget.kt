/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.entity.LivingEntity

class EntityWidget(val entity: LivingEntity, val point: Point) : WidgetWithBounds() {
    override fun children(): List<Element> {
        return emptyList()
    }

    var hasErrored = false

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        try {
            if (!hasErrored)
                EntityRenderer.renderEntity(entity, context, point.x, point.y, mouseX.toFloat(), mouseY.toFloat())
        } catch (ex: Exception) {
            EntityRenderer.logger.error("Failed to render constructed entity: $entity", ex)
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

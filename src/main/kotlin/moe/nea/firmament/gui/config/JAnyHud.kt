/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import moe.nea.jarvis.api.JarvisHud
import moe.nea.jarvis.api.JarvisScalable
import kotlinx.serialization.Serializable
import net.minecraft.text.Text

@Serializable
data class HudPosition(
    var x: Double,
    var y: Double,
    var scale: Float,
)


data class HudMeta(
    val position: HudPosition,
    private val label: Text,
    private val width: Int,
    private val height: Int,
) : JarvisScalable, JarvisHud {
    override fun getX(): Double = position.x

    override fun setX(newX: Double) {
        position.x = newX
    }

    override fun getY(): Double = position.y

    override fun setY(newY: Double) {
        position.y = newY
    }

    override fun getLabel(): Text = label

    override fun getWidth(): Int = width

    override fun getHeight(): Int = height

    override fun getScale(): Float = position.scale

    override fun setScale(newScale: Float) {
        position.scale = newScale
    }

}

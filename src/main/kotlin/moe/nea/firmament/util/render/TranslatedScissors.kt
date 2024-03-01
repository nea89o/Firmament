/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.render

import org.joml.Vector4f
import net.minecraft.client.gui.DrawContext

fun DrawContext.enableScissorWithTranslation(x1: Float, y1: Float, x2: Float, y2: Float) {
    val pMat = matrices.peek().positionMatrix
    val target = Vector4f()

    target.set(x1, y1, 0f, 1f)
    target.mul(pMat)
    val scissorX1 = target.x
    val scissorY1 = target.y

    target.set(x2, y2, 0f, 1f)
    target.mul(pMat)
    val scissorX2 = target.x
    val scissorY2 = target.y

    enableScissor(scissorX1.toInt(), scissorY1.toInt(), scissorX2.toInt(), scissorY2.toInt())
}

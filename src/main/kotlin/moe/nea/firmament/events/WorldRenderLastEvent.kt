/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import org.joml.Matrix4f
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Position
import net.minecraft.util.math.Vec3d

/**
 * This event is called after all world rendering is done, but before any GUI rendering (including hand) has been done.
 */
data class WorldRenderLastEvent(
    val matrices: MatrixStack,
    val tickDelta: Float,
    val renderBlockOutline: Boolean,
    val camera: Camera,
    val gameRenderer: GameRenderer,
    val lightmapTextureManager: LightmapTextureManager,
    val vertexConsumers: VertexConsumerProvider.Immediate,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<WorldRenderLastEvent>()
    data class TextRenderCall(val string: String, val position: Position)

    val toRender = mutableListOf<TextRenderCall>(TextRenderCall("Test String", Vec3d(0.0, 0.0, 0.0)))

}

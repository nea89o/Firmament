/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    val positionMatrix: Matrix4f,
    val vertexConsumers: VertexConsumerProvider.Immediate,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<WorldRenderLastEvent>()
    data class TextRenderCall(val string: String, val position: Position)

    val toRender = mutableListOf<TextRenderCall>(TextRenderCall("Test String", Vec3d(0.0, 0.0, 0.0)))

}

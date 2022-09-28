package moe.nea.notenoughupdates.events

import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f

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
) : NEUEvent() {
    companion object : NEUEventBus<WorldRenderLastEvent>()
}

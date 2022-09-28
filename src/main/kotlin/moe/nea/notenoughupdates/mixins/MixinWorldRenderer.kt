package moe.nea.notenoughupdates.mixins

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import moe.nea.notenoughupdates.events.WorldRenderLastEvent

@Mixin(WorldRenderer::class)
class MixinWorldRenderer {

    @Inject(
        method = ["render"],
        at = [At("INVOKE", target = "renderChunkDebugInfo", shift = At.Shift.AFTER)],
    )
    fun onWorldRenderLast(
        matrices: MatrixStack,
        tickDelta: Float,
        arg2: Long,
        renderBlockOutline: Boolean,
        camera: Camera,
        gameRenderer: GameRenderer,
        lightmapTextureManager: LightmapTextureManager,
        positionMatrix: Matrix4f,
        ci: CallbackInfo
    ) {
        val event = WorldRenderLastEvent(
            matrices, tickDelta, renderBlockOutline,
            camera, gameRenderer, lightmapTextureManager,
            positionMatrix
        )
        WorldRenderLastEvent.publish(event)
    }

}

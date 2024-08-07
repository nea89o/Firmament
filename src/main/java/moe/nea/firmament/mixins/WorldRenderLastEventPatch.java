

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.WorldRenderLastEvent;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.tick.TickManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRenderLastEventPatch {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V", shift = At.Shift.BEFORE))
    public void onWorldRenderLast(
        RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
        LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2,
        CallbackInfo ci, @Local MatrixStack matrixStack
    ) {
        var event = new WorldRenderLastEvent(
            matrixStack, tickCounter, renderBlockOutline,
            camera, gameRenderer, lightmapTextureManager,
            this.bufferBuilders.getEntityVertexConsumers()
        );
        WorldRenderLastEvent.Companion.publish(event);
    }
}

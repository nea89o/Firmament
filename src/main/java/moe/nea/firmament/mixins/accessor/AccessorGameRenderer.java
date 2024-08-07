

package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface AccessorGameRenderer {
    @Invoker("getFov")
    double getFov_firmament(Camera camera, float tickDelta, boolean changingFov);
}

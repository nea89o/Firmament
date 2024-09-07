package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import moe.nea.firmament.events.RegisterCustomShadersEvent;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class InjectCustomShaderPrograms {

    @Inject(method = "loadPrograms",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;loadBlurPostProcessor(Lnet/minecraft/resource/ResourceFactory;)V",
            shift = At.Shift.AFTER))
    void addFirmamentShaders(
        ResourceFactory resourceFactory, CallbackInfo ci,
        @Local(index = 3) List<Pair<ShaderProgram, Consumer<ShaderProgram>>> list
    ) {
        var event = new RegisterCustomShadersEvent(list, resourceFactory);
        RegisterCustomShadersEvent.Companion.publish(event);
    }
}

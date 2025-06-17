package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.events.WorldMouseMoveEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class DispatchMouseInputEventsPatch {
	@WrapWithCondition(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
	public boolean onRotatePlayer(ClientPlayerEntity instance, double deltaX, double deltaY) {
		var event = WorldMouseMoveEvent.Companion.publish(new WorldMouseMoveEvent(deltaX, deltaY));
		return !event.getCancelled();
	}
}

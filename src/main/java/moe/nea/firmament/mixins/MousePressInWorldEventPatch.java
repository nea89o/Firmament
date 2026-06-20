package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.WorldKeyboardEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MousePressInWorldEventPatch {
	@WrapWithCondition(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	public boolean onKeyBoardInWorld(InputConstants.Key key, @Local(argsOnly = true, name = "rawButtonInfo") MouseButtonInfo rawButtonInfo) { // TODO: handle modified mouse click instead
		var event = WorldKeyboardEvent.Companion.publish(new WorldKeyboardEvent(GenericInputAction.of(rawButtonInfo),
			InputModifiers.of(rawButtonInfo)));
		return !event.getCancelled();
	}
}

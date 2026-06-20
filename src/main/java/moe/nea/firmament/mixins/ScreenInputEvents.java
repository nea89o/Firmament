package moe.nea.firmament.mixins;

import moe.nea.firmament.events.HandledScreenKeyPressedEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenInputEvents {
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent(
			(Screen) (Object) this,
			GenericInputAction.of(event),
			InputModifiers.of(event))).getCancelled()) {
			cir.setReturnValue(true);
		}
	}

	@SuppressWarnings("MissingUnique")
	public boolean onMouseClicked$firmament(MouseButtonEvent click, boolean doubled) {
		return HandledScreenKeyPressedEvent.Companion.publish(
			new HandledScreenKeyPressedEvent((Screen) (Object) this,
				GenericInputAction.mouse(click), InputModifiers.current())).getCancelled();
	}
}

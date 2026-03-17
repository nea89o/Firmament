package moe.nea.firmament.mixins.compat;

import moe.nea.firmament.events.HandledScreenKeyPressedEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Publishes HandledScreenKeyPressedEvent for the REI overlay screen.
 * This ensures that keyboard shortcuts work even when viewing REI recipe/usage screens.
 * Fires AFTER REI processes the key, so REI's built-in shortcuts take priority.
 */
@Mixin(targets = "me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl")
@Pseudo
public class PublishREIOverlayKeyPressEvent {
	@Inject(method = {"method_25404", "keyPressed" }, at = @At("RETURN"), require = 0)
	public void onKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		// Only publish event if REI didn't handle the key
		if (!cir.getReturnValue()) {
			Screen currentScreen = Minecraft.getInstance().screen;
			if (currentScreen != null) {
				HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent(
					currentScreen,
					GenericInputAction.of(input),
					InputModifiers.of(input)));
			}
		}
	}
}

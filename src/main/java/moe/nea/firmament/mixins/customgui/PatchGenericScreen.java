package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import moe.nea.firmament.util.customgui.CustomGui;
import moe.nea.firmament.util.customgui.HasCustomGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class PatchGenericScreen implements HasCustomGui {

	@Shadow
	public int width;
	@Shadow
	public int height;

	@Shadow
	protected abstract void init();

	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	protected abstract void extractMenuBackground(GuiGraphicsExtractor graphics);

	@Shadow
	protected abstract void extractBlurredBackground(GuiGraphicsExtractor graphics);

	@Shadow
	protected abstract void extractPanorama(GuiGraphicsExtractor graphics, float a);

	@Shadow
	public abstract void extractTransparentBackground(GuiGraphicsExtractor graphics);

	@Shadow
	public abstract boolean isInGameUi();

	@Unique
	private @Nullable CustomGui override;

	@Nullable
	@Override
	public CustomGui getCustomGui_Firmament() {
		return override;
	}

	@Override
	public void setCustomGui_Firmament(@Nullable CustomGui gui) {
		this.override = gui;
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (override != null)
			override.render(graphics, a, mouseX, mouseY);
	}

	@WrapWithCondition(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
	private boolean replaceBackgroundRendering(Screen instance, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (override != null) {
			extractBackgroundCopy(graphics, mouseX, mouseY, a);
			return false;
		}
		return true;
	}

	/// Copy of [Screen#extractBackground(GuiGraphicsExtractor, int, int, float)], but without inheritance
	@Unique
	private void extractBackgroundCopy(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (this.isInGameUi()) {
			this.extractTransparentBackground(graphics);
		} else {
			if (this.minecraft.level == null) {
				this.extractPanorama(graphics, a);
			}

			this.extractBlurredBackground(graphics);
			this.extractMenuBackground(graphics);
		}

		this.minecraft.gui.hud.extractDeferredSubtitles();
	}

	@SuppressWarnings("MissingUnique")
	public boolean mouseScrolled_firmament(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return override != null && override.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@SuppressWarnings("MissingUnique")
	public boolean keyReleased_firmament(KeyEvent input) {
		if (HandledScreenKeyReleasedEvent.Companion.publish(new HandledScreenKeyReleasedEvent(
			(Screen) (Object) this,
			GenericInputAction.of(input),
			InputModifiers.of(input))).getCancelled())
			return true;
		return override != null && override.keyReleased(input);
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/AbstractContainerEventHandler;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"), cancellable = true)
	public void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (override != null)
			cir.setReturnValue(override.keyPressed(event));
	}

	@SuppressWarnings("MissingUnique")
	public boolean charTyped_firmament(CharacterEvent input) {
		return override != null && override.charTyped(input);
	}

	@SuppressWarnings("MissingUnique")
	public boolean mouseClicked_firmament_customGui(MouseButtonEvent click, boolean doubled) {
		return override != null && override.mouseClick(click, doubled);
	}

	@SuppressWarnings("MissingUnique")
	public boolean mouseReleased_firmament(MouseButtonEvent click) {
		return override != null && override.mouseReleased(click);
	}

	@SuppressWarnings("MissingUnique")
	public boolean mouseDragged_firmament(MouseButtonEvent event, double dx, double dy) {
		return override != null && override.mouseDragged(event, dx, dy);
	}

	@Inject(method = "init()V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (override != null) {
			override.onInit();
		}
	}

	@Inject(at = @At("HEAD"), method = "onClose", cancellable = true)
	private void onVoluntaryExit(CallbackInfo ci) {
		if (override != null) {
			if (!override.onVoluntaryExit())
				ci.cancel();
		}
	}


	// TODO: prevent extraction in superclass (Screen)
}

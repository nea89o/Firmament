
package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import moe.nea.firmament.util.customgui.CoordRememberingSlot;
import moe.nea.firmament.util.customgui.CustomGui;
import moe.nea.firmament.util.customgui.HasCustomGui;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class PatchHandledScreen<T extends AbstractContainerMenu> extends Screen implements HasCustomGui {
	@Shadow
	@Final
	protected T menu;
	@Shadow
	protected int leftPos;
	@Shadow
	protected int topPos;
	@Shadow
	protected int imageHeight;
	@Shadow
	protected int imageWidth;
	@Unique
	public CustomGui override;
	@Unique
	public boolean hasRememberedSlots = false;
	@Unique
	private int originalBackgroundWidth;
	@Unique
	private int originalBackgroundHeight;

	protected PatchHandledScreen(Component title) {
		super(title);
	}

	@Nullable
	@Override
	public CustomGui getCustomGui_Firmament() {
		return override;
	}

	@Override
	public void setCustomGui_Firmament(@Nullable CustomGui gui) {
		if (this.override != null) {
			imageHeight = originalBackgroundHeight;
			imageWidth = originalBackgroundWidth;
		}
		if (gui != null) {
			originalBackgroundHeight = imageHeight;
			originalBackgroundWidth = imageWidth;
		}
		this.override = gui;
	}

	public boolean mouseScrolled_firmament(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return override != null && override.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	public boolean keyReleased_firmament(KeyEvent input) {
		if (HandledScreenKeyReleasedEvent.Companion.publish(new HandledScreenKeyReleasedEvent(
			(AbstractContainerScreen<?>) (Object) this,
			GenericInputAction.of(input),
			InputModifiers.of(input))).getCancelled())
			return true;
		return override != null && override.keyReleased(input);
	}

	public boolean charTyped_firmament(CharacterEvent input) {
		return override != null && override.charTyped(input);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (override != null) {
			override.onInit();
		}
	}

	@Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
	private void onDrawForeground(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
		if (override != null && !override.shouldDrawForeground())
			ci.cancel();
	}


	@WrapOperation(
		method = "renderSlots",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V"))
	private void beforeSlotRender(AbstractContainerScreen instance, GuiGraphics guiGraphics, Slot slot, int i, int j, Operation<Void> original) {
		if (override != null) {
			override.beforeSlotRender(guiGraphics, slot);
		}
		original.call(instance, guiGraphics, slot, i, j);
		if (override != null) {
			override.afterSlotRender(guiGraphics, slot);
		}
	}

	@Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
	public void onIsClickOutsideBounds(
		double mouseX, double mouseY, int left, int top,
		CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isClickOutsideBounds(mouseX, mouseY));
		}
	}

	@Inject(method = "isHovering(IIIIDD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointWithinBounds(x + this.leftPos, y + this.topPos, width, height, pointX, pointY));
		}
	}

	@Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointOverSlot(slot, this.leftPos, this.topPos, pointX, pointY));
		}
	}

	@Inject(method = "renderBackground", at = @At("HEAD"))
	public void moveSlots(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (override != null) {
			for (Slot slot : menu.slots) {
				if (!hasRememberedSlots) {
					((CoordRememberingSlot) slot).rememberCoords_firmament();
				}
				override.moveSlot(slot);
			}
			hasRememberedSlots = true;
		} else {
			if (hasRememberedSlots) {
				for (Slot slot : menu.slots) {
					((CoordRememberingSlot) slot).restoreCoords_firmament();
				}
				hasRememberedSlots = false;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "onClose", cancellable = true)
	private void onVoluntaryExit(CallbackInfo ci) {
		if (override != null) {
			if (!override.onVoluntaryExit())
				ci.cancel();
		}
	}

	@WrapWithCondition(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
	public boolean preventDrawingBackground(AbstractContainerScreen instance, GuiGraphics drawContext, float delta, int mouseX, int mouseY) {
		if (override != null) {
			override.render(drawContext, delta, mouseX, mouseY);
		}
		return override == null;
	}

	@WrapOperation(
		method = "mouseClicked",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
	public boolean overrideMouseClicks(AbstractContainerScreen instance, MouseButtonEvent click, boolean doubled, Operation<Boolean> original) {
		if (override != null) {
			if (override.mouseClick(click, doubled))
				return true;
		}
		return original.call(instance, click, doubled);
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	public void overrideMouseDrags(MouseButtonEvent click, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseDragged(click, offsetX, offsetY))
				cir.setReturnValue(true);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void overrideKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.keyPressed(input)) {
				cir.setReturnValue(true);
			}
		}
	}


	@Inject(
		method = "mouseReleased",
		at = @At("HEAD"), cancellable = true)
	public void overrideMouseReleases(MouseButtonEvent click, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseReleased(click))
				cir.setReturnValue(true);
		}
	}
}

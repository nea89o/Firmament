
package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import moe.nea.firmament.util.customgui.CoordRememberingSlot;
import moe.nea.firmament.util.customgui.CustomGui;
import moe.nea.firmament.util.customgui.HasCustomGui;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	@Final
	@Shadow
	protected int imageHeight;
	@Final
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

	@SuppressWarnings("MissingUnique")
	public boolean mouseScrolled_firmament(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return override != null && override.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@SuppressWarnings("MissingUnique")
	public boolean keyReleased_firmament(KeyEvent input) {
		if (HandledScreenKeyReleasedEvent.Companion.publish(new HandledScreenKeyReleasedEvent(
			(AbstractContainerScreen<?>) (Object) this,
			GenericInputAction.of(input),
			InputModifiers.of(input))).getCancelled())
			return true;
		return override != null && override.keyReleased(input);
	}

	@SuppressWarnings("MissingUnique")
	public boolean charTyped_firmament(CharacterEvent input) {
		return override != null && override.charTyped(input);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (override != null) {
			override.onInit();
		}
	}

	@Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
	private void onDrawForeground(GuiGraphicsExtractor graphics, int xm, int ym, CallbackInfo ci) {
		if (override != null && !override.shouldDrawForeground())
			ci.cancel();
	}


	@WrapOperation(
		method = "extractSlots",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V"))
	private void beforeSlotRender(AbstractContainerScreen<?> instance, GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
		if (override != null) {
			override.beforeSlotRender(graphics, slot);
		}
		original.call(instance, graphics, slot, mouseX, mouseY);
		if (override != null) {
			override.afterSlotRender(graphics, slot);
		}
	}

	@Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
	public void onIsClickOutsideBounds(
		double mx, double my, int xo, int yo,
		CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isClickOutsideBounds(mx, my));
		}
	}

	@Inject(method = "isHovering(IIIIDD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointWithinBounds(int left, int top, int w, int h, double xm, double ym, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointWithinBounds(left + this.leftPos, top + this.topPos, w, h, xm, ym));
		}
	}

	@Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointOverSlot(Slot slot, double xm, double ym, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointOverSlot(slot, this.leftPos, this.topPos, xm, ym));
		}
	}

	@Inject(method = "extractContents", at = @At("HEAD"))
	public void moveSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
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
// TODO: prevent extraction in superclass (Screen)
//	@WrapWithCondition(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FII)V"))
//	public boolean preventDrawingBackground(AbstractContainerScreen instance, GuiGraphicsExtractor drawContext, float delta, int mouseX, int mouseY) {
//		if (override != null) {
//			override.render(drawContext, delta, mouseX, mouseY);
//		}
//		return override == null;
//	}

	@WrapOperation(
		method = "mouseClicked",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
	public boolean overrideMouseClicks(AbstractContainerScreen<?> instance, MouseButtonEvent click, boolean doubled, Operation<Boolean> original) {
		if (override != null) {
			if (override.mouseClick(click, doubled))
				return true;
		}
		return original.call(instance, click, doubled);
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	public void overrideMouseDrags(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseDragged(event, dx, dy))
				cir.setReturnValue(true);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void overrideKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.keyPressed(event)) {
				cir.setReturnValue(true);
			}
		}
	}


	@Inject(
		method = "mouseReleased",
		at = @At("HEAD"), cancellable = true)
	public void overrideMouseReleases(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseReleased(event))
				cir.setReturnValue(true);
		}
	}
}

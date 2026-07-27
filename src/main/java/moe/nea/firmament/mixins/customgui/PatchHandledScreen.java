

package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.util.customgui.CoordRememberingSlot;
import moe.nea.firmament.util.customgui.CustomGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
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
public abstract class PatchHandledScreen<T extends AbstractContainerMenu> extends PatchGenericScreen {
	@Shadow
	@Final
	protected T menu;

	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Unique
	private int savedImageWidth;
	@Unique
	private int savedImageHeight;
	@Unique
	private boolean savedImage;

	@Shadow
	protected int imageWidth;

	@Shadow
	protected int imageHeight;

	protected PatchHandledScreen() {
		throw new RuntimeException();
	}

	@Override
	public void setCustomGui_Firmament(@Nullable CustomGui gui) {
		super.setCustomGui_Firmament(gui);
		fixSize();
	}

	@Unique
	public void fixSize() {
		if (!savedImage) {
			savedImageHeight = imageHeight;
			savedImageWidth = imageWidth;
			savedImage = true;
		}
		var override = getCustomGui_Firmament();
		imageWidth = override != null ? override.getBounds().getFirst().width : savedImageWidth;
		imageHeight = override != null ? override.getBounds().getFirst().height : savedImageHeight;
		this.leftPos = (this.width - imageWidth) / 2;
		this.topPos = (this.height - imageHeight) / 2;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		fixSize();
		var override = getCustomGui_Firmament();
		if (override != null) {
			override.onInit();
		}
	}

	@WrapOperation(
		method = "extractSlots",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V"))
	private void beforeSlotRender(AbstractContainerScreen<?> instance, GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
		var override = getCustomGui_Firmament();
		if (override != null) {
			override.beforeSlotRender(graphics, slot);
		}
		original.call(instance, graphics, slot, mouseX, mouseY);
		if (override != null) {
			override.afterSlotRender(graphics, slot);
		}
	}

	@WrapWithCondition(
		method = "extractContents",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractLabels(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
	private boolean beforeSlotRender(AbstractContainerScreen<T> instance, GuiGraphicsExtractor graphics, int xm, int ym) {
		return getCustomGui_Firmament() == null;
	}

	@Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
	public void onIsClickOutsideBounds(
		double mx, double my, int xo, int yo,
		CallbackInfoReturnable<Boolean> cir) {
		var override = getCustomGui_Firmament();
		if (override != null) {
			cir.setReturnValue(override.isClickOutsideBounds(mx, my));
		}
	}

	@Inject(method = "isHovering(IIIIDD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointWithinBounds(int left, int top, int w, int h, double xm, double ym, CallbackInfoReturnable<Boolean> cir) {
		var override = getCustomGui_Firmament();
		if (override != null) {
			cir.setReturnValue(override.isPointWithinBounds(left + this.leftPos, top + this.topPos, w, h, xm, ym));
		}
	}


	@Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
	public void onIsPointOverSlot(Slot slot, double xm, double ym, CallbackInfoReturnable<Boolean> cir) {
		var override = getCustomGui_Firmament();
		if (override != null) {
			cir.setReturnValue(override.isPointOverSlot(slot, this.leftPos, this.topPos, xm, ym));
		}
	}

	@Unique
	private boolean hasRememberedSlots;

	@Inject(method = "extractContents", at = @At("HEAD"))
	public void moveSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		var override = getCustomGui_Firmament();
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
}

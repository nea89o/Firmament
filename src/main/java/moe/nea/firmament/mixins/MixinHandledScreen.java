



package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.*;
import moe.nea.firmament.events.HandledScreenClickEvent;
import moe.nea.firmament.keybindings.GenericInputAction;
import moe.nea.firmament.keybindings.InputModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractContainerScreen.class, priority = 990)
public abstract class MixinHandledScreen<T extends AbstractContainerMenu> {

	@Shadow
	@Final
	protected T menu;

	@Shadow
	public abstract T getMenu();

	@Unique
	Inventory playerInventory;

	@Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("TAIL"))
	public void savePlayerInventory(AbstractContainerMenu menu, Inventory inventory, Component title, int imageWidth, int imageHeight, CallbackInfo ci) {
		this.playerInventory = inventory;
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		var self = (AbstractContainerScreen<?>) (Object) this;
		var clickEvent = new HandledScreenClickEvent(self, event.x(), event.y(), event.button());
		var keyEvent = new HandledScreenKeyReleasedEvent(self, GenericInputAction.mouse(event), InputModifiers.of(event.modifiers()));
		if (HandledScreenClickEvent.Companion.publish(clickEvent).getCancelled()
			|| HandledScreenKeyReleasedEvent.Companion.publish(keyEvent).getCancelled()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "extractContents", at = @At("HEAD"))
	public void onAfterRenderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		HandledScreenForegroundEvent.Companion.publish(new HandledScreenForegroundEvent((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, a));
	}

	@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClickedSlot(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
		if (slotId == -999 && getMenu() != null && containerInput == ContainerInput.PICKUP) { // -999 is code for "clicked outside the main window"
			ItemStack cursorStack = getMenu().getCarried();
			if (IsSlotProtectedEvent.shouldBlockInteraction(slot, ContainerInput.THROW, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE, cursorStack)) {
				ci.cancel();
				return;
			}
		}
		if (IsSlotProtectedEvent.shouldBlockInteraction(slot, containerInput, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE)) {
			ci.cancel();
			return;
		}
		if (containerInput == ContainerInput.SWAP && 0 <= buttonNum && buttonNum < 9) {
			if (IsSlotProtectedEvent.shouldBlockInteraction(new Slot(playerInventory, buttonNum, 0, 0), containerInput, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE)) {
				ci.cancel();
			}
		}
	}


	@WrapOperation(method = "extractSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V"))
	public void onDrawSlots(AbstractContainerScreen<?> instance, GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
		var before = new SlotRenderEvents.Before(graphics, slot);
		SlotRenderEvents.Before.Companion.publish(before);
		original.call(instance, graphics, slot, mouseX, mouseY);
		var after = new SlotRenderEvents.After(graphics, slot);
		SlotRenderEvents.After.Companion.publish(after);
	}
}

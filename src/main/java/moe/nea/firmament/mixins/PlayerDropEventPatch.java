

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.IsSlotProtectedEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class PlayerDropEventPatch extends PlayerEntity {
	public PlayerDropEventPatch() {
		super(null, null, 0, null);
	}

	@Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
	public void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
		Slot fakeSlot = new Slot(getInventory(), getInventory().getSelectedSlot(), 0, 0);
		if (IsSlotProtectedEvent.shouldBlockInteraction(fakeSlot, SlotActionType.THROW, IsSlotProtectedEvent.MoveOrigin.DROP_FROM_HOTBAR)) {
			cir.setReturnValue(false);
		}
	}
}

package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public class MixinPlayerScreenHandler {

	@Unique
	private static final int OFF_HAND_SLOT = 40;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void removeOffhandSlot(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHideOffHand()) {
			PlayerScreenHandler self = (PlayerScreenHandler) (Object) this;
			self.slots.removeIf(slot -> slot.getIndex() == OFF_HAND_SLOT);
		}
	}
}

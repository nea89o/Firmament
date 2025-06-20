package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerInventory.class, priority = 999)
public class MixinPlayerInventory {

	@Unique
	private static final int OFF_HAND_SLOT = 40;

	@Inject(method = "setStack", at = @At("HEAD"), cancellable = true)
	private void cancelSetOffhand(int slot, ItemStack stack, CallbackInfo ci) {
		if (slot == OFF_HAND_SLOT && Fixes.TConfig.INSTANCE.getHideOffHand()) ci.cancel();
	}

}

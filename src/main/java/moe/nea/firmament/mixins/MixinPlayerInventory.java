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

	@Inject(method = "getStack", at = @At("HEAD"), cancellable = true)
	private void cancelGetOffhand(int slot, CallbackInfoReturnable<ItemStack> cir) {
		if (slot == OFF_HAND_SLOT && Fixes.TConfig.INSTANCE.getHideOffHand()) cir.setReturnValue(ItemStack.EMPTY);
	}

	@Inject(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
	private void cancelRemoveStack(int slot, CallbackInfoReturnable<ItemStack> cir) {
		if (slot == OFF_HAND_SLOT && Fixes.TConfig.INSTANCE.getHideOffHand()) cir.setReturnValue(ItemStack.EMPTY);
	}

	@Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
	private void cancelRemoveStackWithAmount(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
		if (slot == OFF_HAND_SLOT && Fixes.TConfig.INSTANCE.getHideOffHand()) cir.setReturnValue(ItemStack.EMPTY);
	}

}

package moe.nea.firmament.mixins;

import moe.nea.firmament.util.mc.TolerantRegistriesOps;
import net.minecraft.core.HolderOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HolderOwner.class)
public interface TolerateFirmamentTolerateRegistryOwners<T> {
	@Inject(method = "canSerializeIn", at = @At("HEAD"), cancellable = true)
	private void equalTolerantRegistryOwners(HolderOwner<T> context, CallbackInfoReturnable<Boolean> cir) {
		if (context instanceof TolerantRegistriesOps.TolerantOwner<?>) {
			cir.setReturnValue(true);
		}
	}
}

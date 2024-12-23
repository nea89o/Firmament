package moe.nea.firmament.mixins;

import moe.nea.firmament.util.mc.TolerantRegistriesOps;
import net.minecraft.registry.entry.RegistryEntryOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryEntryOwner.class)
public interface TolerateFirmamentTolerateRegistryOwners<T> {
	@Inject(method = "ownerEquals", at = @At("HEAD"), cancellable = true)
	private void equalTolerantRegistryOwners(RegistryEntryOwner<T> other, CallbackInfoReturnable<Boolean> cir) {
		if (other instanceof TolerantRegistriesOps.TolerantOwner<?>) {
			cir.setReturnValue(true);
		}
	}
}

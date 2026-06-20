package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class MixinPlayerScreenHandler {

	@Unique
	private static final int OFF_HAND_SLOT = 40;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void moveOffHandSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHideOffHand()) {
			InventoryMenu self = (InventoryMenu) (Object) this;
			self.slots.stream()
				.filter(slot -> slot.getContainerSlot() == OFF_HAND_SLOT)
				.forEach(slot -> {
					slot.x = -1000;
					slot.y = -1000;
				});
		}
	}
}

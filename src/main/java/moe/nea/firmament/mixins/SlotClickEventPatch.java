
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import moe.nea.firmament.events.SlotClickEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class SlotClickEventPatch {

    @Inject(method = "handleContainerInput", at = @At(value = "FIELD", target =
		"Lnet/minecraft/world/inventory/AbstractContainerMenu;slots:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.GETFIELD))
    private void onSlotClickSaveSlot(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci, @Local(name = "containerMenu") AbstractContainerMenu containerMenu, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotNum && slotNum < containerMenu.slots.size()) {
            slotContent.set(containerMenu.getSlot(slotNum).getItem().copy());
        }
    }

    @Inject(method = "handleContainerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onSlotClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci, @Local(name = "containerMenu") AbstractContainerMenu containerMenu, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotNum && slotNum < containerMenu.slots.size()) {
            SlotClickEvent.Companion.publish(new SlotClickEvent(
                containerMenu.getSlot(slotNum),
                slotContent.get(),
				buttonNum,
				containerInput
            ));
        }
    }
}

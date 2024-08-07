
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import moe.nea.firmament.events.SlotClickEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class SlotClickEventPatch {

    @Inject(method = "clickSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/screen/ScreenHandler;slots:Lnet/minecraft/util/collection/DefaultedList;", opcode = Opcodes.GETFIELD))
    private void onSlotClickSaveSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, @Local ScreenHandler handler, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotId && slotId < handler.slots.size()) {
            slotContent.set(handler.getSlot(slotId).getStack().copy());
        }
    }

    @Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void onSlotClick(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, @Local ScreenHandler handler, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotId && slotId < handler.slots.size()) {
            SlotClickEvent.Companion.publish(new SlotClickEvent(
                handler.getSlot(slotId),
                slotContent.get(),
                button,
                actionType
            ));
        }
    }
}

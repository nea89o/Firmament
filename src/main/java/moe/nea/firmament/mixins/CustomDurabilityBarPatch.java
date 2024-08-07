
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import moe.nea.firmament.util.DurabilityBarEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public class CustomDurabilityBarPatch {
    @WrapOperation(
        method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z")
    )
    private boolean onIsItemBarVisible(
        ItemStack instance, Operation<Boolean> original,
        @Share("barOverride") LocalRef<DurabilityBarEvent.DurabilityBar> barOverride
    ) {
        if (original.call(instance))
            return true;
        DurabilityBarEvent event = new DurabilityBarEvent(instance);
        DurabilityBarEvent.Companion.publish(event);
        barOverride.set(event.getBarOverride());
        return barOverride.get() != null;
    }

    @WrapOperation(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemBarStep()I"))
    private int overrideItemStep(
        ItemStack instance, Operation<Integer> original,
        @Share("barOverride") LocalRef<DurabilityBarEvent.DurabilityBar> barOverride
    ) {
        if (barOverride.get() != null)
            return Math.round(barOverride.get().getPercentage() * 13);
        return original.call(instance);
    }

    @WrapOperation(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemBarColor()I"))
    private int overrideItemColor(
        ItemStack instance, Operation<Integer> original,
        @Share("barOverride") LocalRef<DurabilityBarEvent.DurabilityBar> barOverride
    ) {
        if (barOverride.get() != null)
            return barOverride.get().getColor().getColor();
        return original.call(instance);
    }
}

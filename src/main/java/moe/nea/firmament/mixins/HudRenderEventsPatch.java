

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.HotbarItemRenderEvent;
import moe.nea.firmament.events.HudRenderEvent;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudRenderEventsPatch {
    @Inject(method = "renderSleepOverlay", at = @At(value = "HEAD"))
    public void renderCallBack(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HudRenderEvent.Companion.publish(new HudRenderEvent(context, tickCounter));
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    public void onRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (stack != null && !stack.isEmpty())
            HotbarItemRenderEvent.Companion.publish(new HotbarItemRenderEvent(stack, context, x, y, tickCounter));
    }

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void hideStatusEffects(CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffects()) ci.cancel();
	}

}

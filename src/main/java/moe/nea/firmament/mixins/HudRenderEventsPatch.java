



package moe.nea.firmament.mixins;

import moe.nea.firmament.events.HotbarItemRenderEvent;
import moe.nea.firmament.events.HudRenderEvent;
import moe.nea.firmament.features.fixes.Fixes;
import moe.nea.firmament.util.SBData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudRenderEventsPatch {
	@Inject(method = "extractSleepOverlay", at = @At(value = "HEAD"))
	public void renderCallBack(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		HudRenderEvent.Companion.publish(new HudRenderEvent(graphics, deltaTracker));
	}

	@Inject(method = "extractSlot", at = @At("HEAD"))
	public void onRenderHotbarItem(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
		if (itemStack != null && !itemStack.isEmpty())
			HotbarItemRenderEvent.Companion.publish(new HotbarItemRenderEvent(itemStack, graphics, x, y, deltaTracker));
	}

	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	public void hideStatusEffects(CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffectsHud() && SBData.INSTANCE.isOnSkyblock()) ci.cancel();
	}

}

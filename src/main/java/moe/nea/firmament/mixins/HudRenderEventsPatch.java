/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.HotbarItemRenderEvent;
import moe.nea.firmament.events.HudRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudRenderEventsPatch {
    @Inject(method = "renderSleepOverlay", at = @At(value = "HEAD"))
    public void renderCallBack(DrawContext context, float tickDelta, CallbackInfo ci) {
        HudRenderEvent.Companion.publish(new HudRenderEvent(context, tickDelta));
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    public void onRenderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (stack != null && !stack.isEmpty())
            HotbarItemRenderEvent.Companion.publish(new HotbarItemRenderEvent(stack, context, x, y, tickDelta));
    }
}

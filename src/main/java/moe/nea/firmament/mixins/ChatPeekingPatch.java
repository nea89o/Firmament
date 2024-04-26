/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatPeekingPatch {

    @ModifyVariable(method = "render", at = @At(value = "HEAD"), index = 5, argsOnly = true)
    public boolean onGetChatHud(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }

    @ModifyExpressionValue(method = "getHeight()I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    public boolean onGetChatHudHeight(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }

}

/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.mojang.authlib.SignatureState;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import moe.nea.firmament.features.fixes.Fixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class PropertySignatureIgnorePatchForSession {
    @Inject(method = "getPropertySignatureState", at = @At("HEAD"), cancellable = true, remap = false)
    public void markEverythingAsSigned(Property property, CallbackInfoReturnable<SignatureState> cir) {
        // Due to https://github.com/inglettronald/DulkirMod-Fabric/blob/22a3fc514a080fbe31f76f9ba7e85c36d8d0f67f/src/main/java/com/dulkirfabric/mixin/YggdrasilMinecraftSessionServiceMixin.java
        // we sadly need to inject here too. Dulkirmod is very eager to early on mark a signature as unsigned
        // and we want the opposite
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            cir.setReturnValue(SignatureState.SIGNED);
        }
    }
}

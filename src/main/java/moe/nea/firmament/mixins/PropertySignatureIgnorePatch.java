/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.mojang.authlib.properties.Property;
import moe.nea.firmament.features.fixes.Fixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.security.PublicKey;

@Mixin(value = Property.class, remap = false)
public class PropertySignatureIgnorePatch {
    @Inject(method = "isSignatureValid", cancellable = true, at = @At("HEAD"), remap = false)
    public void onValidateSignature(PublicKey publicKey, CallbackInfoReturnable<Boolean> cir) {
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "signature", cancellable = true, at = @At("HEAD"), remap = false)
    public void returnEmptySignatureInsteadOfNull(CallbackInfoReturnable<String> cir) {
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            cir.setReturnValue("");
        }
    }

    @Inject(method = "hasSignature", cancellable = true, at = @At("HEAD"), remap = false)
    public void onHasSignature(CallbackInfoReturnable<Boolean> cir) {
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            cir.setReturnValue(true);
        }
    }
}

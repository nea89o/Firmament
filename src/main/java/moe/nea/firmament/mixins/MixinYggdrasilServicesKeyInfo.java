/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import moe.nea.firmament.features.fixes.Fixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class MixinYggdrasilServicesKeyInfo {
    @Inject(method = "validateProperty", at = @At("HEAD"), cancellable = true, remap = false)
    public void validate(Property property, CallbackInfoReturnable<Boolean> cir) {
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            cir.setReturnValue(true);
        }
    }
}

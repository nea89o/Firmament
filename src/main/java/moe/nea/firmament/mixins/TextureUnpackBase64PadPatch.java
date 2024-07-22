/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import moe.nea.firmament.util.Base64Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class TextureUnpackBase64PadPatch {
    @ModifyExpressionValue(method = "unpackTextures",
        remap = false,
        at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/properties/Property;value()Ljava/lang/String;"))
    private String base64PadTexture(String original) {
        if (original.length() % 4 == 0) return original;
        return Base64Util.INSTANCE.padToValidBase64(original);
    }
}

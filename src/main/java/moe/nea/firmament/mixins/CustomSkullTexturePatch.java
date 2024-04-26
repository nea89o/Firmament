/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.mojang.authlib.GameProfile;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.component.type.ProfileComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockEntityRenderer.class)
public class CustomSkullTexturePatch {
    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private static void onGetRenderLayer(SkullBlock.SkullType type, ProfileComponent profile, CallbackInfoReturnable<RenderLayer> cir) {
        CustomSkyBlockTextures.INSTANCE.modifySkullTexture(type, profile, cir);
    }
}

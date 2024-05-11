/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomGlobalTextures;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class GlobalModelOverridePatch {

    @Shadow
    public abstract ItemModels getModels();

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void overrideGlobalModel(
        ItemStack stack, World world, LivingEntity entity,
        int seed, CallbackInfoReturnable<BakedModel> cir) {
        CustomGlobalTextures.replaceGlobalModel(this.getModels(), stack, cir);
    }
}

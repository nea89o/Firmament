/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;


import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorTexturePatch<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {
    @Unique
    private ItemStack lastRenderedArmorItem;

    @Unique
    private boolean foundCustomTexture;

    @WrapWithCondition(method = "renderArmorParts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private boolean preventRenderingLeatherArmorColor(BipedEntityModel instance, MatrixStack matrixStack,
                                                      VertexConsumer vertexConsumer, int light, int uv,
                                                      float r, float g, float b, float a,
                                                      @Local(argsOnly = true) @Nullable String overlay) {
        if (overlay != null) return true;
        if (foundCustomTexture) return true;
        var customOverlayTexture = CustomSkyBlockTextures.INSTANCE.getArmorTexture(this.lastRenderedArmorItem, false, "overlay");
        return customOverlayTexture == null;
    }

    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private void onBeforeRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci,
                                     @Local ItemStack itemStack) {
        this.lastRenderedArmorItem = itemStack;
    }

    @Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(ArmorItem item, boolean secondLayer, String overlay, CallbackInfoReturnable<Identifier> cir) {
        if (this.lastRenderedArmorItem == null) return;
        var armorTexture = CustomSkyBlockTextures.INSTANCE.getArmorTexture(this.lastRenderedArmorItem, secondLayer, overlay);
        if (armorTexture != null) {
            cir.setReturnValue(armorTexture);
            this.foundCustomTexture = true;
        } else {
            this.foundCustomTexture = false;
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.render.GenderArmorLayer;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GenderArmorLayer.class)
@Pseudo
public class PatchArmorTexturesInGenderMod {
    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getMaterial()Lnet/minecraft/registry/entry/RegistryEntry;"))
    private RegistryEntry<ArmorMaterial> replaceArmorMaterial(ArmorItem instance, Operation<RegistryEntry<ArmorMaterial>> original, @Local ItemStack chestplate) {
        var entry = original.call(instance);
        var overrides = CustomGlobalArmorOverrides.overrideArmor(chestplate);
        if (overrides == null)
            return entry;
        var material = entry.value();
        return RegistryEntry.of(new ArmorMaterial(
            material.defense(),
            material.enchantability(),
            material.equipSound(),
            material.repairIngredient(),
            overrides,
            material.toughness(),
            material.knockbackResistance()
        ));
    }
}

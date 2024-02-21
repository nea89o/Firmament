/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedOverrideData;
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate;
import moe.nea.firmament.features.texturepack.ModelOverrideData;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ModelOverrideList.class)
public class TestForFirmamentOverridePredicatesPatch {

    @ModifyArg(method = "<init>(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/List;)V",
        at = @At(
            value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        ))
    public Object onInit(
        Object element,
        @Local ModelOverride modelOverride
    ) {
        var bakedOverride = (ModelOverrideList.BakedOverride) element;
        ((BakedOverrideData) bakedOverride)
            .setFirmamentOverrides(((ModelOverrideData) modelOverride).getFirmamentOverrides());
        return element;
    }

    @ModifyExpressionValue(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ModelOverrideList$BakedOverride;test([F)Z"))
    public boolean testFirmamentOverrides(boolean originalValue,
                                          @Local ModelOverrideList.BakedOverride bakedOverride,
                                          @Local ItemStack stack) {
        if (!originalValue) return false;
        var overrideData = (BakedOverrideData) bakedOverride;
        var overrides = overrideData.getFirmamentOverrides();
        if (overrides == null) return true;
        for (FirmamentModelPredicate firmamentOverride : overrides) {
            if (!firmamentOverride.test(stack))
                return false;
        }
        return true;
    }
}

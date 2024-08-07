
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ArmorFeatureRenderer.class)
public class PatchArmorTexture {
    @WrapOperation(
        method = "renderArmor",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorMaterial;layers()Ljava/util/List;"))
    private List<ArmorMaterial.Layer> overrideLayers(
        ArmorMaterial instance,
        Operation<List<ArmorMaterial.Layer>> original,
        @Local ItemStack itemStack
    ) {
        var overrides = CustomGlobalArmorOverrides.overrideArmor(itemStack);
        if (overrides == null)
            return original.call(instance);
        return overrides;
    }
}

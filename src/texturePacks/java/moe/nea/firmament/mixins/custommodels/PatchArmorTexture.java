
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorFeatureRenderer.class)
public class PatchArmorTexture {
	@ModifyExpressionValue(
		method = "renderArmor",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
	private Object overrideLayers(
		Object original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) EquipmentSlot slot
	) {
		var overrides = CustomGlobalArmorOverrides.overrideArmor(itemStack, slot);
		return overrides.orElse((EquippableComponent) original);
	}
}

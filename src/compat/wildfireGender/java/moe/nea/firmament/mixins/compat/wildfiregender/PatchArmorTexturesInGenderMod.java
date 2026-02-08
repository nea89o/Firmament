package moe.nea.firmament.mixins.compat.wildfiregender;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.render.GenderArmorLayer;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GenderArmorLayer.class)
@Pseudo
public class PatchArmorTexturesInGenderMod {
	@ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	private Object replaceArmorMaterial(Object original, @Local(name = "chestplate") ItemStack chestplate) {
		var overrides = CustomGlobalArmorOverrides.overrideArmor(chestplate, EquipmentSlot.CHEST);
		return overrides.orElse((Equippable) original);
	}
}

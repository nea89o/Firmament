package moe.nea.firmament.mixins.compat.wildfiregender;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.render.GenderArmorLayer;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GenderArmorLayer.class)
@Pseudo
public class PatchArmorTexturesInGenderMod {
	@ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
	private Object replaceArmorMaterial(Object original, @Local ItemStack chestplate) {
		var overrides = CustomGlobalArmorOverrides.overrideArmor(chestplate, EquipmentSlot.CHEST);
		return overrides.orElse((EquippableComponent) original);
	}
}

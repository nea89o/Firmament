
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomGlobalArmorOverrides;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ArmorFeatureRenderer.class)
public class PatchArmorTexture {
	@WrapOperation(
		method = "renderArmor",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/EquippableComponent;model()Ljava/util/Optional;"))
	private Optional<Identifier> overrideLayers(
		EquippableComponent instance, Operation<Optional<Identifier>> original, @Local(argsOnly = true) ItemStack itemStack
	) {
		// TODO: check that all armour items are naturally equippable and have the equppable component. otherwise our call here will not be reached.
		var overrides = CustomGlobalArmorOverrides.overrideArmor(itemStack);
		return overrides.or(() -> original.call(instance));
	}
}

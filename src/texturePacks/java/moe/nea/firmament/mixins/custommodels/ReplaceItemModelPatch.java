package moe.nea.firmament.mixins.custommodels;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.CustomItemModelEvent;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelManager.class)
public class ReplaceItemModelPatch {
	@WrapOperation(
		method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
	private Object replaceItemModelByIdentifier(ItemStack instance, ComponentType componentType, Operation<Object> original) {
		var override = CustomItemModelEvent.getModelIdentifier(instance);
		if (override != null)
			return override;
		return original.call(instance, componentType);
	}
}

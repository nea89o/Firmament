package moe.nea.firmament.mixins.custommodels;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.CustomItemModelEvent;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.MissingItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ItemModelManager.class)
public class ReplaceItemModelPatch {
	@Shadow
	@Final
	private Function<Identifier, ItemModel> modelGetter;

	@Unique
	private boolean hasModel(Identifier identifier) {
		return !(modelGetter.apply(identifier) instanceof MissingItemModel);
	}

	@WrapOperation(
		method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
	private Object replaceItemModelByIdentifier(ItemStack instance, ComponentType componentType, Operation<Object> original) {
		var override = CustomItemModelEvent.getModelIdentifier(instance);
		if (override != null && hasModel(override)) {
			return override;
		}
		return original.call(instance, componentType);
	}
}

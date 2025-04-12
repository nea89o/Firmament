package moe.nea.firmament.mixins.custommodels;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.CustomItemModelEvent;
import moe.nea.firmament.util.mc.IntrospectableItemModelManager;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.MissingItemModel;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(ItemModelManager.class)
public class ReplaceItemModelPatch implements IntrospectableItemModelManager {
	@Shadow
	@Final
	private Function<Identifier, ItemModel> modelGetter;

	@WrapOperation(
		method = "update",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
	private Object replaceItemModelByIdentifier(ItemStack instance, ComponentType componentType, Operation<Object> original) {
		var override = CustomItemModelEvent.getModelIdentifier(instance, this);
		if (override != null && hasModel_firmament(override)) {
			return override;
		}
		return original.call(instance, componentType);
	}

	@Override
	public boolean hasModel_firmament(@NotNull Identifier identifier) {
		return !(modelGetter.apply(identifier) instanceof MissingItemModel);
	}
}

package moe.nea.firmament.mixins.custommodels;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.CustomItemModelEvent;
import moe.nea.firmament.util.mc.IntrospectableItemModelManager;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(ItemModelResolver.class)
public class ReplaceItemModelPatch implements IntrospectableItemModelManager {
	@Shadow
	@Final
	private Function<Identifier, ItemModel> modelGetter;

	@WrapOperation(
		method = "appendItemLayers",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	private Object replaceItemModelByIdentifier(ItemStack instance, DataComponentType componentType, Operation<Object> original) {
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

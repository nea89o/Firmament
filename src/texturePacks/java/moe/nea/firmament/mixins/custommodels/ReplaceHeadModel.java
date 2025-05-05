package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.HeadModelChooser;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class ReplaceHeadModel<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
	@Shadow
	@Final
	protected ItemModelManager itemModelResolver;

	@Unique
	private ItemRenderState tempRenderState = new ItemRenderState();

	@Inject(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void replaceHeadModel(
		T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci
	) {
		var headItemStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);

		HeadModelChooser.INSTANCE.getIS_CHOOSING_HEAD_MODEL().set(true);
		tempRenderState.clear();
		this.itemModelResolver.updateForLivingEntity(tempRenderState, headItemStack, ItemDisplayContext.HEAD, livingEntity);
		HeadModelChooser.INSTANCE.getIS_CHOOSING_HEAD_MODEL().set(false);

		if (HeadModelChooser.HasExplicitHeadModelMarker.cast(tempRenderState)
			.isExplicitHeadModel_Firmament()) {
			livingEntityRenderState.wearingSkullType = null;
			var temp = livingEntityRenderState.headItemRenderState;
			livingEntityRenderState.headItemRenderState = tempRenderState;
			tempRenderState = temp;
		}
	}
}

package moe.nea.firmament.mixins.render.entitytints;

import moe.nea.firmament.events.EntityRenderTintEvent;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dispatches {@link EntityRenderTintEvent} to collect additional render state used by {@link ChangeColorOfLivingEntities}
 */
@Mixin(EntityRenderer.class)
public class InjectIntoRenderState<T extends Entity, S extends EntityRenderState> {

	@Inject(
		method = "updateRenderState",
		at = @At("RETURN"))
	private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
		var renderState = EntityRenderTintEvent.HasTintRenderState.cast(state);
		renderState.reset_firmament();
		var tintEvent = new EntityRenderTintEvent(
			entity,
			renderState
		);
		EntityRenderTintEvent.Companion.publish(tintEvent);
	}
}

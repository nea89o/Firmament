package moe.nea.firmament.events

import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import moe.nea.firmament.events.EntityRenderTintEvent.Companion.overlayOverride
import moe.nea.firmament.util.render.TintedOverlayTexture

/**
 * Change the tint color of a [LivingEntity]
 */
class EntityRenderTintEvent(
    val entity: Entity,
    val renderState: HasTintRenderState
) : FirmamentEvent.Cancellable() {
	init {
		if (entity !is LivingEntity) {
			cancel()
		}
	}

	companion object : FirmamentEventBus<EntityRenderTintEvent>() {
		/**
		 * Static variable containing an override for [GameRenderer.getOverlayTexture]. Should be only set briefly.
		 *
		 * This variable only affects render layers that naturally make use of the overlay texture, have proper overlay UVs set (`overlay u != 0`), and have a shader that makes use of the overlay (does not have the `NO_OVERLAY` flag set in its json definition).
		 *
		 * Currently supported layers: [net.minecraft.client.render.entity.equipment.EquipmentRenderer], [net.minecraft.client.render.entity.model.PlayerEntityModel], as well as some others naturally.
		 *
		 * @see TintedOverlayTexture
		 * @see moe.nea.firmament.mixins.render.entitytints.ReplaceOverlayTexture
		 * @see moe.nea.firmament.mixins.render.entitytints.UseOverlayableEquipmentRenderer
		 * @see moe.nea.firmament.mixins.render.entitytints.UseOverlayableHeadFeatureRenderer
		 */
		@JvmField
		var overlayOverride: OverlayTexture? = null
	}

	@Suppress("PropertyName", "FunctionName")
	interface HasTintRenderState {
		/**
		 * Multiplicative tint applied before the overlay.
		 */
		var tint_firmament: Int

		/**
		 * Must be set for [tint_firmament] to have any effect.
		 */
		var hasTintOverride_firmament: Boolean

		// TODO: allow for more specific selection of which layers get tinted
		/**
		 * Specify a [TintedOverlayTexture] to be used. This does not apply to render layers not using the overlay texture.
		 * @see overlayOverride
		 */
		var overlayTexture_firmament: TintedOverlayTexture?
		fun reset_firmament()

		companion object {
			@JvmStatic
			fun cast(state: EntityRenderState): HasTintRenderState {
				return state as HasTintRenderState
			}
		}
	}

}

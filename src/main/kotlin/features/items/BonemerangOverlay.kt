package moe.nea.firmament.features.items

import me.shedaniel.math.Color
import moe.nea.jarvis.api.Point
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.math.Box
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.events.EntityRenderTintEvent
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.TintedOverlayTexture
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.tr

object BonemerangOverlay : FirmamentFeature {
	override val identifier: String
		get() = "bonemerang-overlay"

	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var bonemerangOverlay by toggle("bonemerang-overlay") { false }
		val bonemerangOverlayHud by position("bonemerang-overlay-hud", 80, 10) { Point(0.1, 1.0) }
		var highlightHitEntities by toggle("highlight-hit-entities") { false }
	}

	@Subscribe
	fun onInit(event: ClientStartedEvent) {
	}

	override val config: ManagedConfig
		get() = TConfig

	fun getEntities(): MutableSet<LivingEntity> {
		val entities = mutableSetOf<LivingEntity>()
		val camera = MC.camera as? PlayerEntity ?: return entities
		val player = MC.player ?: return entities
		val world = player.world ?: return entities

		val cameraPos = camera.eyePos
		val rayDirection = camera.rotationVector.normalize()
		val endPos = cameraPos.add(rayDirection.multiply(15.0))
		val foundEntities = world.getOtherEntities(camera, Box(cameraPos, endPos).expand(1.0))

		for (entity in foundEntities) {
			if (entity !is LivingEntity || entity is ArmorStandEntity || entity.isInvisible) continue
			val hitResult = entity.boundingBox.expand(0.35).raycast(cameraPos, endPos).orElse(null)
			if (hitResult != null) entities.add(entity)
		}

		return entities
	}


	val throwableWeapons = listOf(
		SkyBlockItems.BONE_BOOMERANG, SkyBlockItems.STARRED_BONE_BOOMERANG,
		SkyBlockItems.TRIBAL_SPEAR,
	)


	@Subscribe
	fun onEntityRender(event: EntityRenderTintEvent) {
		if (!TConfig.highlightHitEntities) return
		if (MC.stackInHand.skyBlockId !in throwableWeapons) return

		val entities = getEntities()
		if (entities.isEmpty()) return
		if (event.entity !in entities) return

		val tintOverlay by lazy {
			TintedOverlayTexture().setColor(Color.ofOpaque(Formatting.BLUE.colorValue!!))
		}

		event.renderState.overlayTexture_firmament = tintOverlay
	}


	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.bonemerangOverlay) return
		if (MC.stackInHand.skyBlockId !in throwableWeapons) return

		val entities = getEntities()

		it.context.matrices.push()
		TConfig.bonemerangOverlayHud.applyTransformations(it.context.matrices)
		it.context.drawText(
			MC.font, String.format(
				tr(
					"firmament.bonemerang-overlay.bonemerang-overlay.display", "Bonemerang Targets: %s"
				).string, entities.size
			), 0, 0, -1, true
		)
		it.context.matrices.pop()
	}
}

package moe.nea.firmament.features.items

import me.shedaniel.math.Color
import org.joml.Vector2i
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.ChatFormatting
import net.minecraft.world.phys.AABB
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.EntityRenderTintEvent
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.render.TintedOverlayTexture
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.tr

object BonemerangOverlay {
	val identifier: String
		get() = "bonemerang-overlay"

	@Config
	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var bonemerangOverlay by toggle("bonemerang-overlay") { false }
		val bonemerangOverlayHud by position("bonemerang-overlay-hud", 80, 10) { Vector2i() }
		var highlightHitEntities by toggle("highlight-hit-entities") { false }
	}

	fun getEntities(): MutableSet<LivingEntity> {
		val entities = mutableSetOf<LivingEntity>()
		val camera = MC.camera as? Player ?: return entities
		val player = MC.player ?: return entities
		val world = player.level ?: return entities

		val cameraPos = camera.eyePosition
		val rayDirection = camera.lookAngle.normalize()
		val endPos = cameraPos.add(rayDirection.scale(15.0))
		val foundEntities = world.getEntities(camera, AABB(cameraPos, endPos).inflate(1.0))

		for (entity in foundEntities) {
			if (entity !is LivingEntity || entity is ArmorStand || entity.isInvisible) continue
			val hitResult = entity.boundingBox.inflate(0.35).clip(cameraPos, endPos).orElse(null)
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
		if (MC.stackInHand.accessor().skyBlockId !in throwableWeapons) return

		val entities = getEntities()
		if (entities.isEmpty()) return
		if (event.entity !in entities) return

		val tintOverlay by lazy {
			TintedOverlayTexture().setColor(Color.ofOpaque(ChatFormatting.BLUE.color!!))
		}

		event.renderState.overlayTexture_firmament = tintOverlay
	}


	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.bonemerangOverlay) return
		if (MC.stackInHand.accessor().skyBlockId !in throwableWeapons) return

		val entities = getEntities()

		it.context.pose().pushMatrix()
		TConfig.bonemerangOverlayHud.applyTransformations(it.context.pose())
		it.context.text(
			MC.font, String.format(
				tr(
					"firmament.bonemerang-overlay.bonemerang-overlay.display", "Bonemerang Targets: %s"
				).string, entities.size
			), 0, 0, -1, true
		)
		it.context.pose().popMatrix()
	}
}

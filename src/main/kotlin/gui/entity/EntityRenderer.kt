package moe.nea.firmament.gui.entity

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.util.Identifier
import net.minecraft.world.World
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.iterate
import moe.nea.firmament.util.openFirmamentResource
import moe.nea.firmament.util.render.enableScissorWithTranslation

object EntityRenderer {
	val fakeWorld: World get() = MC.lastWorld!!
	private fun <T : Entity> t(entityType: EntityType<T>): () -> T {
		return { entityType.create(fakeWorld, SpawnReason.LOAD)!! }
	}

	val entityIds: Map<String, () -> LivingEntity> = mapOf(
		"Armadillo" to t(EntityType.ARMADILLO),
		"ArmorStand" to t(EntityType.ARMOR_STAND),
		"Axolotl" to t(EntityType.AXOLOTL),
		"BREEZE" to t(EntityType.BREEZE),
		"Bat" to t(EntityType.BAT),
		"Bee" to t(EntityType.BEE),
		"Blaze" to t(EntityType.BLAZE),
		"CaveSpider" to t(EntityType.CAVE_SPIDER),
		"Chicken" to t(EntityType.CHICKEN),
		"Cod" to t(EntityType.COD),
		"Cow" to t(EntityType.COW),
		"Creaking" to t(EntityType.CREAKING),
		"Creeper" to t(EntityType.CREEPER),
		"Dolphin" to t(EntityType.DOLPHIN),
		"Donkey" to t(EntityType.DONKEY),
		"Dragon" to t(EntityType.ENDER_DRAGON),
		"Drowned" to t(EntityType.DROWNED),
		"Eisengolem" to t(EntityType.IRON_GOLEM),
		"Enderman" to t(EntityType.ENDERMAN),
		"Endermite" to t(EntityType.ENDERMITE),
		"Evoker" to t(EntityType.EVOKER),
		"Fox" to t(EntityType.FOX),
		"Frog" to t(EntityType.FROG),
		"Ghast" to t(EntityType.GHAST),
		"Giant" to t(EntityType.GIANT),
		"GlowSquid" to t(EntityType.GLOW_SQUID),
		"Goat" to t(EntityType.GOAT),
		"Guardian" to t(EntityType.GUARDIAN),
		"Horse" to t(EntityType.HORSE),
		"Husk" to t(EntityType.HUSK),
		"Illusioner" to t(EntityType.ILLUSIONER),
		"LLama" to t(EntityType.LLAMA),
		"MagmaCube" to t(EntityType.MAGMA_CUBE),
		"Mooshroom" to t(EntityType.MOOSHROOM),
		"Mule" to t(EntityType.MULE),
		"Ocelot" to t(EntityType.OCELOT),
		"Panda" to t(EntityType.PANDA),
		"Phantom" to t(EntityType.PHANTOM),
		"Pig" to t(EntityType.PIG),
		"Piglin" to t(EntityType.PIGLIN),
		"PiglinBrute" to t(EntityType.PIGLIN_BRUTE),
		"Pigman" to t(EntityType.ZOMBIFIED_PIGLIN),
		"Pillager" to t(EntityType.PILLAGER),
		"Player" to { makeGuiPlayer(fakeWorld) },
		"PolarBear" to t(EntityType.POLAR_BEAR),
		"Pufferfish" to t(EntityType.PUFFERFISH),
		"Rabbit" to t(EntityType.RABBIT),
		"Salmom" to t(EntityType.SALMON),
		"Sheep" to t(EntityType.SHEEP),
		"Shulker" to t(EntityType.SHULKER),
		"Silverfish" to t(EntityType.SILVERFISH),
		"Skeleton" to t(EntityType.SKELETON),
		"Slime" to t(EntityType.SLIME),
		"Sniffer" to t(EntityType.SNIFFER),
		"Snowman" to t(EntityType.SNOW_GOLEM),
		"Spider" to t(EntityType.SPIDER),
		"Squid" to t(EntityType.SQUID),
		"Stray" to t(EntityType.STRAY),
		"Strider" to t(EntityType.STRIDER),
		"Tadpole" to t(EntityType.TADPOLE),
		"TropicalFish" to t(EntityType.TROPICAL_FISH),
		"Turtle" to t(EntityType.TURTLE),
		"Vex" to t(EntityType.VEX),
		"Villager" to t(EntityType.VILLAGER),
		"Vindicator" to t(EntityType.VINDICATOR),
		"Warden" to t(EntityType.WARDEN),
		"Witch" to t(EntityType.WITCH),
		"Wither" to t(EntityType.WITHER),
		"WitherSkeleton" to t(EntityType.WITHER_SKELETON),
		"Wolf" to t(EntityType.WOLF),
		"Zoglin" to t(EntityType.ZOGLIN),
		"Zombie" to t(EntityType.ZOMBIE),
		"ZombieVillager" to t(EntityType.ZOMBIE_VILLAGER)
	)
	val entityModifiers: Map<String, EntityModifier> = mapOf(
		"playerdata" to ModifyPlayerSkin,
		"equipment" to ModifyEquipment,
		"riding" to ModifyRiding,
		"charged" to ModifyCharged,
		"witherdata" to ModifyWither,
		"invisible" to ModifyInvisible,
		"age" to ModifyAge,
		"horse" to ModifyHorse,
		"name" to ModifyName,
	)

	fun applyModifiers(entityId: String, modifiers: List<JsonObject>): LivingEntity? {
		val entityType = ErrorUtil.notNullOr(entityIds[entityId], "Could not create entity with id $entityId") {
			return null
		}
		var entity = ErrorUtil.catch("") { entityType() }.or { return null }
		for (modifierJson in modifiers) {
			val modifier = ErrorUtil.notNullOr(
				modifierJson["type"]?.asString?.let(entityModifiers::get),
				"Could not create entity with id $entityId. Failed to apply modifier $modifierJson") { return null }
			entity = modifier.apply(entity, modifierJson)
		}
		return entity
	}

	fun constructEntity(info: JsonObject): LivingEntity? {
		val modifiers = (info["modifiers"] as JsonArray?)?.map { it.asJsonObject } ?: emptyList()
		val entityType = ErrorUtil.notNullOr(info["entity"]?.asString, "Missing entity type on entity object") {
			return null
		}
		return applyModifiers(entityType, modifiers)
	}

	private val gson = Gson()
	fun constructEntity(location: Identifier): LivingEntity? {
		return constructEntity(
			gson.fromJson(
				location.openFirmamentResource().bufferedReader(), JsonObject::class.java
			)
		)
	}

	fun renderEntity(
		entity: LivingEntity,
		renderContext: DrawContext,
		posX: Int,
		posY: Int,
		// TODO: Add width, height properties here
		width: Double,
		height: Double,
		mouseX: Double,
		mouseY: Double,
		entityScale: Double = (height - 10.0) / 2.0
	) {
		var bottomOffset = 0.0
		var currentEntity = entity
		val maxSize = entity.iterate { it.firstPassenger as? LivingEntity }
			.map { it.height }
			.sum()
		while (true) {
			currentEntity.age = MC.player?.age ?: 0
			drawEntity(
				renderContext,
				posX,
				posY,
				(posX + width).toInt(),
				(posY + height).toInt(),
				minOf(2F / maxSize, 1F) * entityScale,
				-bottomOffset,
				mouseX,
				mouseY,
				currentEntity
			)
			val next = currentEntity.firstPassenger as? LivingEntity ?: break
			bottomOffset += currentEntity.getPassengerRidingPos(next).y.toFloat() * 0.75F
			currentEntity = next
		}
	}


	fun drawEntity(
		context: DrawContext,
		x1: Int,
		y1: Int,
		x2: Int,
		y2: Int,
		size: Double,
		bottomOffset: Double,
		mouseX: Double,
		mouseY: Double,
		entity: LivingEntity
	) {
		context.enableScissorWithTranslation(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
		val centerX = (x1 + x2) / 2f
		val centerY = (y1 + y2) / 2f
		val hw = (x2 - x1) / 2
		val hh = (y2 - y1) / 2
		val targetYaw = atan(((centerX - mouseX) / hw)).toFloat()
		val targetPitch = atan(((centerY - mouseY) / hh)).toFloat()
		val rotateToFaceTheFront = Quaternionf().rotateZ(Math.PI.toFloat())
		val rotateToFaceTheCamera = Quaternionf().rotateX(targetPitch * 20.0f * (Math.PI.toFloat() / 180))
		rotateToFaceTheFront.mul(rotateToFaceTheCamera)
		val oldBodyYaw = entity.bodyYaw
		val oldYaw = entity.yaw
		val oldPitch = entity.pitch
		val oldPrevHeadYaw = entity.lastHeadYaw
		val oldHeadYaw = entity.headYaw
		entity.bodyYaw = 180.0f + targetYaw * 20.0f
		entity.yaw = 180.0f + targetYaw * 40.0f
		entity.pitch = -targetPitch * 20.0f
		entity.headYaw = entity.yaw
		entity.lastHeadYaw = entity.yaw
		val vector3f = Vector3f(0.0f, (entity.height / 2.0f + bottomOffset).toFloat(), 0.0f)
		InventoryScreen.drawEntity(
			context,
			centerX,
			centerY,
			size.toFloat(),
			vector3f,
			rotateToFaceTheFront,
			rotateToFaceTheCamera,
			entity
		)
		entity.bodyYaw = oldBodyYaw
		entity.yaw = oldYaw
		entity.pitch = oldPitch
		entity.lastHeadYaw = oldPrevHeadYaw
		entity.headYaw = oldHeadYaw
		context.disableScissor()
	}


}

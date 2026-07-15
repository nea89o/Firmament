package moe.nea.firmament.gui.entity

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.shedaniel.math.Dimension
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.level.Level
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.iterate
import moe.nea.firmament.util.openFirmamentResource
import moe.nea.firmament.util.render.enableScissorWithTranslation

object EntityRenderer {
	val fakeWorld: Level get() = MC.lastWorld!!
	private fun <T : Entity> t(entityType: EntityType<T>): () -> T {
		return { entityType.create(fakeWorld, EntitySpawnReason.LOAD)!! }
	}

	val entityIds: Map<String, () -> LivingEntity> = mapOf(
		"Armadillo" to t(EntityTypes.ARMADILLO),
		"ArmorStand" to t(EntityTypes.ARMOR_STAND),
		"Axolotl" to t(EntityTypes.AXOLOTL),
		"Bat" to t(EntityTypes.BAT),
		"Bee" to t(EntityTypes.BEE),
		"Blaze" to t(EntityTypes.BLAZE),
		"Bogged" to t(EntityTypes.BOGGED),
		"Breeze" to t(EntityTypes.BREEZE),
		"CaveSpider" to t(EntityTypes.CAVE_SPIDER),
		"Chicken" to t(EntityTypes.CHICKEN),
		"Cod" to t(EntityTypes.COD),
		"Cow" to t(EntityTypes.COW),
		"Creaking" to t(EntityTypes.CREAKING),
		"Creeper" to t(EntityTypes.CREEPER),
		"Dolphin" to t(EntityTypes.DOLPHIN),
		"Donkey" to t(EntityTypes.DONKEY),
		"Dragon" to t(EntityTypes.ENDER_DRAGON),
		"Drowned" to t(EntityTypes.DROWNED),
		"Eisengolem" to t(EntityTypes.IRON_GOLEM),
		"Enderman" to t(EntityTypes.ENDERMAN),
		"Endermite" to t(EntityTypes.ENDERMITE),
		"Evoker" to t(EntityTypes.EVOKER),
		"Fox" to t(EntityTypes.FOX),
		"Frog" to t(EntityTypes.FROG),
		"Ghast" to t(EntityTypes.GHAST),
		"Giant" to t(EntityTypes.GIANT),
		"GlowSquid" to t(EntityTypes.GLOW_SQUID),
		"Goat" to t(EntityTypes.GOAT),
		"Guardian" to t(EntityTypes.GUARDIAN),
		"Horse" to t(EntityTypes.HORSE),
		"Husk" to t(EntityTypes.HUSK),
		"Illusioner" to t(EntityTypes.ILLUSIONER),
		"LLama" to t(EntityTypes.LLAMA),
		"MagmaCube" to t(EntityTypes.MAGMA_CUBE),
		"Mooshroom" to t(EntityTypes.MOOSHROOM),
		"Mule" to t(EntityTypes.MULE),
		"Ocelot" to t(EntityTypes.OCELOT),
		"Panda" to t(EntityTypes.PANDA),
		"Phantom" to t(EntityTypes.PHANTOM),
		"Pig" to t(EntityTypes.PIG),
		"Piglin" to t(EntityTypes.PIGLIN),
		"PiglinBrute" to t(EntityTypes.PIGLIN_BRUTE),
		"Pigman" to t(EntityTypes.ZOMBIFIED_PIGLIN),
		"Pillager" to t(EntityTypes.PILLAGER),
		"Player" to { makeGuiPlayer(fakeWorld) },
		"PolarBear" to t(EntityTypes.POLAR_BEAR),
		"Pufferfish" to t(EntityTypes.PUFFERFISH),
		"Rabbit" to t(EntityTypes.RABBIT),
		"Salmom" to t(EntityTypes.SALMON),
		"Salmon" to t(EntityTypes.SALMON),
		"Sheep" to t(EntityTypes.SHEEP),
		"Shulker" to t(EntityTypes.SHULKER),
		"Silverfish" to t(EntityTypes.SILVERFISH),
		"Skeleton" to t(EntityTypes.SKELETON),
		"Slime" to t(EntityTypes.SLIME),
		"Sniffer" to t(EntityTypes.SNIFFER),
		"Snowman" to t(EntityTypes.SNOW_GOLEM),
		"Spider" to t(EntityTypes.SPIDER),
		"Squid" to t(EntityTypes.SQUID),
		"Stray" to t(EntityTypes.STRAY),
		"Strider" to t(EntityTypes.STRIDER),
		"Tadpole" to t(EntityTypes.TADPOLE),
		"TropicalFish" to t(EntityTypes.TROPICAL_FISH),
		"Turtle" to t(EntityTypes.TURTLE),
		"Vex" to t(EntityTypes.VEX),
		"Villager" to t(EntityTypes.VILLAGER),
		"Vindicator" to t(EntityTypes.VINDICATOR),
		"Warden" to t(EntityTypes.WARDEN),
		"Witch" to t(EntityTypes.WITCH),
		"Wither" to t(EntityTypes.WITHER),
		"WitherSkeleton" to t(EntityTypes.WITHER_SKELETON),
		"Wolf" to t(EntityTypes.WOLF),
		"Zoglin" to t(EntityTypes.ZOGLIN),
		"Zombie" to t(EntityTypes.ZOMBIE),
		"ZombieVillager" to t(EntityTypes.ZOMBIE_VILLAGER)
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
				"Could not create entity with id $entityId. Failed to apply modifier $modifierJson"
			) { return null }
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
		renderContext: GuiGraphicsExtractor,
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
			.map { it.bbHeight }
			.sum()
		while (true) {
			currentEntity.tickCount = MC.player?.tickCount ?: 0
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
			bottomOffset += currentEntity.getPassengerRidingPosition(next).y.toFloat() * 0.75F
			currentEntity = next
		}
	}


	fun drawEntity(
		context: GuiGraphicsExtractor,
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
		InventoryScreen.extractEntityInInventoryFollowsMouse(
			context,
			x1, y1,
			x2, y2,
			size.toInt(),
			bottomOffset.toFloat(),
			mouseX.toFloat(),
			mouseY.toFloat(),
			entity
		)
		context.disableScissor()
	}

	val defaultSize = Dimension(50, 80)
}

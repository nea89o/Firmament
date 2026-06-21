package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.mc.RequiresComponents
import moe.nea.firmament.util.mc.arbitraryUUID
import moe.nea.firmament.util.mc.setEncodedSkullOwner

object ModifyEquipment : EntityModifier {
	val names = mapOf(
		"hand" to EquipmentSlot.MAINHAND,
		"helmet" to EquipmentSlot.HEAD,
		"chestplate" to EquipmentSlot.CHEST,
		"leggings" to EquipmentSlot.LEGS,
		"feet" to EquipmentSlot.FEET,
	)

	override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
		names.forEach { (key, slot) ->
			info[key]?.let {
				entity.setItemSlot(slot, createItem(it.asString))
			}
		}
		return entity
	}

	@OptIn(ExpensiveItemCacheApi::class, RequiresComponents::class)
	private fun createItem(item: String): ItemStack {
		val split = item.split("#")
		if (split.size != 2) return SBItemStack(SkyblockId(item)).asImmutableItemStack().upgrade()
		val (type, data) = split
		return when (type) {
			"SKULL" -> ItemStack(Items.PLAYER_HEAD).also { it.setEncodedSkullOwner(arbitraryUUID, data) }
			"LEATHER_LEGGINGS" -> coloredLeatherArmor(Items.LEATHER_LEGGINGS, data)
			"LEATHER_BOOTS" -> coloredLeatherArmor(Items.LEATHER_BOOTS, data)
			"LEATHER_HELMET" -> coloredLeatherArmor(Items.LEATHER_HELMET, data)
			"LEATHER_CHESTPLATE" -> coloredLeatherArmor(Items.LEATHER_CHESTPLATE, data)
			else -> error("Unknown leather piece: $type")
		}
	}

	private fun coloredLeatherArmor(leatherArmor: Item, data: String): ItemStack {
		val stack = ItemStack(leatherArmor)
		stack.set(DataComponents.DYED_COLOR, DyedItemColor(data.toInt(16)))
		return stack
	}
}

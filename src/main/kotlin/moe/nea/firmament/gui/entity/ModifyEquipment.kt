/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import moe.nea.firmament.rei.SBItemStack
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.item.setEncodedSkullOwner
import moe.nea.firmament.util.item.zeroUUID

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
                entity.equipStack(slot, createItem(it.asString))
            }
        }
        return entity
    }

    private fun createItem(item: String): ItemStack {
        val split = item.split("#")
        if (split.size != 2) return SBItemStack(SkyblockId(item)).asImmutableItemStack()
        val (type, data) = split
        return when (type) {
            "SKULL" -> ItemStack(Items.PLAYER_HEAD).also { it.setEncodedSkullOwner(zeroUUID, data) }
            "LEATHER_LEGGINGS" -> coloredLeatherArmor(Items.LEATHER_LEGGINGS, data)
            "LEATHER_BOOTS" -> coloredLeatherArmor(Items.LEATHER_BOOTS, data)
            "LEATHER_HELMET" -> coloredLeatherArmor(Items.LEATHER_HELMET, data)
            "LEATHER_CHESTPLATE" -> coloredLeatherArmor(Items.LEATHER_CHESTPLATE, data)
            else -> error("Unknown leather piece: $type")
        }
    }

    private fun coloredLeatherArmor(leatherArmor: Item, data: String): ItemStack {
        require(leatherArmor is DyeableArmorItem)
        val stack = ItemStack(leatherArmor)
        leatherArmor.setColor(stack, data.toInt(16))
        return stack
    }
}

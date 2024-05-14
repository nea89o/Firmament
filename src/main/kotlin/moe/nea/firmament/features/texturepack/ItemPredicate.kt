/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC

class ItemPredicate(
    val item: Item
) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return stack.item == item
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): ItemPredicate? {
            if (jsonElement is JsonPrimitive && jsonElement.isString) {
                val itemKey = RegistryKey.of(RegistryKeys.ITEM,
                                             Identifier.tryParse(jsonElement.asString)
                                                 ?: return null)
                return ItemPredicate(MC.defaultItems.getOptional(itemKey).getOrNull()?.value() ?: return null)
            }
            return null
        }
    }
}

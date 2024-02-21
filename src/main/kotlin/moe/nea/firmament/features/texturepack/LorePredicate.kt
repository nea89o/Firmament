/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtString

class LorePredicate(val matcher: StringMatcher) : FirmamentModelPredicate {
    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return LorePredicate(StringMatcher.parse(jsonElement))
        }
    }

    override fun test(stack: ItemStack): Boolean {
        val display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY)
        if (!display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE.toInt()))
            return false
        val lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE.toInt())
        return lore.any { matcher.matches(it as NbtString)}
    }
}

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
import moe.nea.firmament.util.item.displayNameAccordingToNbt
import moe.nea.firmament.util.item.loreAccordingToNbt

data class DisplayNamePredicate(val stringMatcher: StringMatcher) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        val display = stack.displayNameAccordingToNbt
        return stringMatcher.matches(display)
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return DisplayNamePredicate(StringMatcher.parse(jsonElement))
        }
    }
}

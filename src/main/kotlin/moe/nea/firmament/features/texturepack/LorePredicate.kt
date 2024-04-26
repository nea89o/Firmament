/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.item.loreAccordingToNbt

class LorePredicate(val matcher: StringMatcher) : FirmamentModelPredicate {
    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return LorePredicate(StringMatcher.parse(jsonElement))
        }
    }

    override fun test(stack: ItemStack): Boolean {
        val lore = stack.loreAccordingToNbt
        return lore.any { matcher.matches(it) }
    }
}

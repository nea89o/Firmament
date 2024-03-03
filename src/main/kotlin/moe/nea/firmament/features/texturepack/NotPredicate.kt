/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.item.ItemStack

class NotPredicate(val children: Array<FirmamentModelPredicate>) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return children.none { it.test(stack) }
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return NotPredicate(CustomModelOverrideParser.parsePredicates(jsonElement as JsonObject).toTypedArray())
        }
    }
}

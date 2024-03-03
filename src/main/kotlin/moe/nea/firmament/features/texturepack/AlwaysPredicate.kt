/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import net.minecraft.item.ItemStack

object AlwaysPredicate : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return true
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return AlwaysPredicate
        }
    }
}

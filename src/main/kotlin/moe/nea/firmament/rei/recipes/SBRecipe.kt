/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei.recipes

import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEURecipe
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.util.SkyblockId

abstract class SBRecipe : Display {
    abstract val neuRecipe: NEURecipe
    override fun getInputEntries(): List<EntryIngredient> {
        return neuRecipe.allInputs
            .filter { it.itemId != NEUIngredient.NEU_SENTINEL_EMPTY }
            .map {
                val entryStack = SBItemEntryDefinition.getEntry(SkyblockId(it.itemId))
                EntryIngredient.of(entryStack)
            }
    }

    override fun getOutputEntries(): List<EntryIngredient> {
        return neuRecipe.allOutputs
            .filter { it.itemId != NEUIngredient.NEU_SENTINEL_EMPTY }
            .map {
                val entryStack = SBItemEntryDefinition.getEntry(SkyblockId(it.itemId))
                EntryIngredient.of(entryStack)
            }
    }
}

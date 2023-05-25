/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.recipes

import io.github.moulberry.repo.data.NEURecipe
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.util.SkyblockId

abstract class SBRecipe() : Display {
    abstract val neuRecipe: NEURecipe
    override fun getInputEntries(): List<EntryIngredient> {
        return neuRecipe.allInputs.map {
            val entryStack = SBItemEntryDefinition.getEntry(SkyblockId(it.itemId))
            EntryIngredient.of(entryStack)
        }
    }

    override fun getOutputEntries(): List<EntryIngredient> {
        return neuRecipe.allOutputs.map {
            val entryStack = SBItemEntryDefinition.getEntry(SkyblockId(it.itemId))
            EntryIngredient.of(entryStack)
        }
    }
}

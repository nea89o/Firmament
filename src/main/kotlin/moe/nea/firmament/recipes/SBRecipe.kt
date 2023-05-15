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

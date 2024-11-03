package moe.nea.firmament.compat.rei.recipes

import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEURecipe
import java.util.Optional
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.entry.EntryIngredient
import net.minecraft.util.Identifier
import moe.nea.firmament.compat.rei.SBItemEntryDefinition
import moe.nea.firmament.util.SkyblockId

abstract class SBRecipe : Display {
	override fun getDisplayLocation(): Optional<Identifier> {
		// In theory, we could return a location for the neuRecipe here. (Something along the lines of neurepo:items/item_id.json/0 for the 0th recipe in the items/item_id.json recipes array).
		return Optional.empty()
	}

	override fun getSerializer(): DisplaySerializer<out Display>? {
		// While returning null here is discouraged, we are fine to do so, since this recipe will never travel through the network
		return null
	}

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

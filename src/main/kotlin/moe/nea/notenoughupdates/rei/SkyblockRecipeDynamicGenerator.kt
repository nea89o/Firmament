package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.client.view.ViewSearchBuilder
import me.shedaniel.rei.api.common.entry.EntryStack
import moe.nea.notenoughupdates.recipes.SBCraftingRecipe
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.skyblockId
import java.util.*

object SkyblockRecipeDynamicGenerator: DynamicDisplayGenerator<SBCraftingRecipe> {
    override fun getRecipeFor(entry: EntryStack<*>): Optional<List<SBCraftingRecipe>> {
        if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
        val item = entry.castValue<NEUItem>()
        val recipes = RepoManager.getRecipesFor(item.skyblockId)
        val craftingRecipes = recipes.filterIsInstance<NEUCraftingRecipe>()
        return Optional.of(craftingRecipes.map { SBCraftingRecipe(it) })
    }

    override fun generate(builder: ViewSearchBuilder): Optional<List<SBCraftingRecipe>> {
        if (SBCraftingRecipe.Category.catIdentifier !in builder.categories) return Optional.empty()
        return Optional.of(
            RepoManager.getAllRecipes().filterIsInstance<NEUCraftingRecipe>().map { SBCraftingRecipe(it) }
                .toList()
        )
    }

    override fun getUsageFor(entry: EntryStack<*>): Optional<List<SBCraftingRecipe>> {
        if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
        val item = entry.castValue<NEUItem>()
        val recipes = RepoManager.getUsagesFor(item.skyblockId)
        val craftingRecipes = recipes.filterIsInstance<NEUCraftingRecipe>()
        return Optional.of(craftingRecipes.map { SBCraftingRecipe(it) })

    }
}

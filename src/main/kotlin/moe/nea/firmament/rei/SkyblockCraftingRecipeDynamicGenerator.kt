

package moe.nea.firmament.rei

import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUForgeRecipe
import io.github.moulberry.repo.data.NEUKatUpgradeRecipe
import io.github.moulberry.repo.data.NEUMobDropRecipe
import io.github.moulberry.repo.data.NEURecipe
import java.util.Optional
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.client.view.ViewSearchBuilder
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryStack
import moe.nea.firmament.rei.recipes.SBCraftingRecipe
import moe.nea.firmament.rei.recipes.SBEssenceUpgradeRecipe
import moe.nea.firmament.rei.recipes.SBForgeRecipe
import moe.nea.firmament.rei.recipes.SBKatRecipe
import moe.nea.firmament.rei.recipes.SBMobDropRecipe
import moe.nea.firmament.repo.EssenceRecipeProvider
import moe.nea.firmament.repo.RepoManager


val SkyblockCraftingRecipeDynamicGenerator =
    neuDisplayGenerator<SBCraftingRecipe, NEUCraftingRecipe> { SBCraftingRecipe(it) }

val SkyblockForgeRecipeDynamicGenerator =
    neuDisplayGenerator<SBForgeRecipe, NEUForgeRecipe> { SBForgeRecipe(it) }

val SkyblockMobDropRecipeDynamicGenerator =
    neuDisplayGenerator<SBMobDropRecipe, NEUMobDropRecipe> { SBMobDropRecipe(it) }

val SkyblockKatRecipeDynamicGenerator =
    neuDisplayGenerator<SBKatRecipe, NEUKatUpgradeRecipe> { SBKatRecipe(it) }
val SkyblockEssenceRecipeDynamicGenerator =
    neuDisplayGenerator<SBEssenceUpgradeRecipe, EssenceRecipeProvider.EssenceUpgradeRecipe> { SBEssenceUpgradeRecipe(it) }

inline fun <D : Display, reified T : NEURecipe> neuDisplayGenerator(crossinline mapper: (T) -> D) =
    object : DynamicDisplayGenerator<D> {
        override fun getRecipeFor(entry: EntryStack<*>): Optional<List<D>> {
            if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
            val item = entry.castValue<SBItemStack>()
            val recipes = RepoManager.getRecipesFor(item.skyblockId)
            val craftingRecipes = recipes.filterIsInstance<T>()
            return Optional.of(craftingRecipes.map(mapper))
        }

        override fun generate(builder: ViewSearchBuilder): Optional<List<D>> {
            if (SBCraftingRecipe.Category.catIdentifier !in builder.categories) return Optional.empty()
            return Optional.of(
                RepoManager.getAllRecipes().filterIsInstance<T>().map { mapper(it) }
                    .toList()
            )
        }

        override fun getUsageFor(entry: EntryStack<*>): Optional<List<D>> {
            if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
            val item = entry.castValue<SBItemStack>()
            val recipes = RepoManager.getUsagesFor(item.skyblockId)
            val craftingRecipes = recipes.filterIsInstance<T>()
            return Optional.of(craftingRecipes.map(mapper))

        }
    }

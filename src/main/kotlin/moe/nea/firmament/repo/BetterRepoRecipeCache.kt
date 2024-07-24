/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEURecipe
import moe.nea.firmament.util.SkyblockId

class BetterRepoRecipeCache(val essenceRecipeProvider: EssenceRecipeProvider) : IReloadable {
    var usages: Map<SkyblockId, Set<NEURecipe>> = mapOf()
    var recipes: Map<SkyblockId, Set<NEURecipe>> = mapOf()

    override fun reload(repository: NEURepository) {
        val usages = mutableMapOf<SkyblockId, MutableSet<NEURecipe>>()
        val recipes = mutableMapOf<SkyblockId, MutableSet<NEURecipe>>()
        val baseRecipes = repository.items.items.values
            .asSequence()
            .flatMap { it.recipes }
        val extraRecipes = essenceRecipeProvider.recipes
        (baseRecipes + extraRecipes)
            .forEach { recipe ->
                recipe.allInputs.forEach { usages.getOrPut(SkyblockId(it.itemId), ::mutableSetOf).add(recipe) }
                recipe.allOutputs.forEach { recipes.getOrPut(SkyblockId(it.itemId), ::mutableSetOf).add(recipe) }
            }
        this.usages = usages
        this.recipes = recipes
    }
}

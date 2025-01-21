package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUNpcShopRecipe
import io.github.moulberry.repo.data.NEURecipe
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyblockId

class BetterRepoRecipeCache(vararg val extraProviders: ExtraRecipeProvider) : IReloadable {
	var usages: Map<SkyblockId, Set<NEURecipe>> = mapOf()
	var recipes: Map<SkyblockId, Set<NEURecipe>> = mapOf()

	override fun reload(repository: NEURepository) {
		val usages = mutableMapOf<SkyblockId, MutableSet<NEURecipe>>()
		val recipes = mutableMapOf<SkyblockId, MutableSet<NEURecipe>>()
		val baseRecipes = repository.items.items.values
			.asSequence()
			.flatMap { it.recipes }
		(baseRecipes + extraProviders.flatMap { it.provideExtraRecipes() })
			.forEach { recipe ->
				if (recipe is NEUNpcShopRecipe) {
					usages.getOrPut(recipe.isSoldBy.skyblockId, ::mutableSetOf).add(recipe)
				}
				recipe.allInputs.forEach { usages.getOrPut(SkyblockId(it.itemId), ::mutableSetOf).add(recipe) }
				recipe.allOutputs.forEach { recipes.getOrPut(SkyblockId(it.itemId), ::mutableSetOf).add(recipe) }
			}
		this.usages = usages
		this.recipes = recipes
	}
}

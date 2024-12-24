package moe.nea.firmament.repo

import io.github.moulberry.repo.data.NEURecipe

interface ExtraRecipeProvider {
	fun provideExtraRecipes(): Iterable<NEURecipe>
}

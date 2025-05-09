package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEURecipe
import me.shedaniel.math.Rectangle
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.repo.SBItemStack

interface GenericRecipeRenderer<T : NEURecipe> {
	fun render(recipe: T, bounds: Rectangle, layouter: RecipeLayouter)
	fun getInputs(recipe: T): Collection<SBItemStack>
	fun getOutputs(recipe: T): Collection<SBItemStack>
	val icon: ItemStack
	val title: Text
	val identifier: Identifier
	fun findAllRecipes(neuRepository: NEURepository): Iterable<T>
	val displayHeight: Int get() = 66
	val typ: Class<T>
}

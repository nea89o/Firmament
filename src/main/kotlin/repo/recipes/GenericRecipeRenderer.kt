package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEURecipe
import me.shedaniel.math.Rectangle
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import moe.nea.firmament.repo.SBItemStack

interface GenericRecipeRenderer<T : Any> {
	fun render(recipe: T, bounds: Rectangle, layouter: RecipeLayouter, mainItem: SBItemStack?)
	fun getInputs(recipe: T): Collection<SBItemStack>
	fun getOutputs(recipe: T): Collection<SBItemStack>
	val icon: ItemStack
	val title: Component
	val identifier: Identifier
	fun findAllRecipes(neuRepository: NEURepository): Iterable<T>
	fun discoverExtraRecipes(neuRepository: NEURepository, itemStack: SBItemStack, mustBeInOutputs: Boolean): Iterable<T> = emptyList()
	val displayHeight: Int get() = 66
	val displayWidth: Int get() = 150
	val typ: Class<T>
}

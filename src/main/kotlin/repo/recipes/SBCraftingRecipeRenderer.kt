package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUCraftingRecipe
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.tr

class SBCraftingRecipeRenderer : GenericRecipeRenderer<NEUCraftingRecipe> {
	override fun render(recipe: NEUCraftingRecipe, bounds: Rectangle, layouter: RecipeLayouter) {
		val point = Point(bounds.centerX - 58, bounds.centerY - 27)
		layouter.createArrow(point.x + 60, point.y + 18)
		for (i in 0 until 3) {
			for (j in 0 until 3) {
				val item = recipe.inputs[i + j * 3]
				layouter.createItemSlot(point.x + 1 + i * 18,
				                        point.y + 1 + j * 18,
				                        SBItemStack(item),
				                        RecipeLayouter.SlotKind.SMALL_INPUT)
			}
		}
		layouter.createItemSlot(
			point.x + 95, point.y + 19,
			SBItemStack(recipe.output),
			RecipeLayouter.SlotKind.BIG_OUTPUT
		)
	}

	override fun getInputs(recipe: NEUCraftingRecipe): Collection<SBItemStack> {
		return recipe.allInputs.mapNotNull { SBItemStack(it) }
	}

	override fun getOutputs(recipe: NEUCraftingRecipe): Collection<SBItemStack> {
		return SBItemStack(recipe.output)?.let(::listOf) ?: emptyList()
	}

	override fun findAllRecipes(neuRepository: NEURepository): Iterable<NEUCraftingRecipe> {
		return neuRepository.items.items.values.flatMap { it.recipes }.filterIsInstance<NEUCraftingRecipe>()
	}

	override val icon: ItemStack = ItemStack(Blocks.CRAFTING_TABLE)
	override val title: Text = tr("firmament.category.crafting", "SkyBlock Crafting")
	override val identifier: Identifier = Firmament.identifier("crafting_recipe")
}

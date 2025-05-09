package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUForgeRecipe
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.tr

object SBForgeRecipeRenderer : GenericRecipeRenderer<NEUForgeRecipe> {
	override fun render(
		recipe: NEUForgeRecipe,
		bounds: Rectangle,
		layouter: RecipeLayouter
	) {
		val arrow = layouter.createArrow(bounds.minX + 90, bounds.minY + 54 - 18 / 2)
		layouter.createTooltip(
			arrow,
			Text.stringifiedTranslatable(
				"firmament.recipe.forge.time",
				recipe.duration.seconds
			)
		)

		val ingredientsCenter = Point(bounds.minX + 49 - 8, bounds.minY + 54 - 8)
		layouter.createFire(ingredientsCenter, 25)
		val count = recipe.inputs.size
		if (count == 1) {
			layouter.createItemSlot(
				ingredientsCenter.x, ingredientsCenter.y - 18,
				SBItemStack(recipe.inputs.single()),
				RecipeLayouter.SlotKind.SMALL_INPUT,
			)
		} else {
			recipe.inputs.forEachIndexed { idx, ingredient ->
				val rad = Math.PI * 2 * idx / count
				layouter.createItemSlot(
					(ingredientsCenter.x + cos(rad) * 30).toInt(), (ingredientsCenter.y + sin(rad) * 30).toInt(),
					SBItemStack(ingredient),
					RecipeLayouter.SlotKind.SMALL_INPUT,
				)
			}
		}
		layouter.createItemSlot(
			bounds.minX + 124, bounds.minY + 46,
			SBItemStack(recipe.outputStack),
			RecipeLayouter.SlotKind.BIG_OUTPUT
		)
	}

	override val displayHeight: Int
		get() = 104

	override fun getInputs(recipe: NEUForgeRecipe): Collection<SBItemStack> {
		return recipe.inputs.mapNotNull { SBItemStack(it) }
	}

	override fun getOutputs(recipe: NEUForgeRecipe): Collection<SBItemStack> {
		return listOfNotNull(SBItemStack(recipe.outputStack))
	}

	override val icon: ItemStack = ItemStack(Blocks.ANVIL)
	override val title: Text = tr("firmament.category.forge", "Forge Recipes")
	override val identifier: Identifier = Firmament.identifier("forge_recipe")

	override fun findAllRecipes(neuRepository: NEURepository): Iterable<NEUForgeRecipe> {
		// TODO: theres gotta be an index for these tbh.
		return neuRepository.items.items.values.flatMap { it.recipes }.filterIsInstance<NEUForgeRecipe>()
	}

	override val typ: Class<NEUForgeRecipe>
		get() = NEUForgeRecipe::class.java
}

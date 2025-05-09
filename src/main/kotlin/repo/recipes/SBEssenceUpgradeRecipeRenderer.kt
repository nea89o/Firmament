package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUForgeRecipe
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.EssenceRecipeProvider
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.tr

object SBEssenceUpgradeRecipeRenderer : GenericRecipeRenderer<EssenceRecipeProvider.EssenceUpgradeRecipe> {
	override fun render(
		recipe: EssenceRecipeProvider.EssenceUpgradeRecipe,
		bounds: Rectangle,
		layouter: RecipeLayouter,
		mainItem: SBItemStack?
	) {
		val sourceItem = mainItem ?: SBItemStack(recipe.itemId)
		layouter.createItemSlot(
			bounds.minX + 12,
			bounds.centerY - 8 - 18 / 2,
			sourceItem.copy(stars = recipe.starCountAfter - 1),
			RecipeLayouter.SlotKind.SMALL_INPUT
			)
		layouter.createItemSlot(
			bounds.minX + 12, bounds.centerY - 8 + 18 / 2,
			SBItemStack(recipe.essenceIngredient),
			RecipeLayouter.SlotKind.SMALL_INPUT
		)
		layouter.createItemSlot(
			bounds.maxX - 12 - 16, bounds.centerY - 8,
			sourceItem.copy(stars = recipe.starCountAfter),
			RecipeLayouter.SlotKind.SMALL_OUTPUT
		)
		val extraItems = recipe.extraItems
		layouter.createArrow(
			bounds.centerX - 24 / 2,
			if (extraItems.isEmpty()) bounds.centerY - 17 / 2
			else bounds.centerY + 18 / 2
		)
		for ((index, item) in extraItems.withIndex()) {
			layouter.createItemSlot(
				bounds.centerX - extraItems.size * 16 / 2 - 2 / 2 + index * 18,
				bounds.centerY - 18 / 2,
				SBItemStack(item),
				RecipeLayouter.SlotKind.SMALL_INPUT,
			)
		}
	}

	override fun getInputs(recipe: EssenceRecipeProvider.EssenceUpgradeRecipe): Collection<SBItemStack> {
		return recipe.allInputs.mapNotNull { SBItemStack(it) }
	}

	override fun getOutputs(recipe: EssenceRecipeProvider.EssenceUpgradeRecipe): Collection<SBItemStack> {
		return listOfNotNull(SBItemStack(recipe.itemId))
	}

	override val icon: ItemStack get() = SBItemStack(SkyblockId("ESSENCE_WITHER")).asImmutableItemStack()
	override val title: Text = tr("firmament.category.essence", "Essence Upgrades")
	override val identifier: Identifier = Firmament.identifier("essence_upgrade")
	override fun findAllRecipes(neuRepository: NEURepository): Iterable<EssenceRecipeProvider.EssenceUpgradeRecipe> {
		return RepoManager.essenceRecipeProvider.recipes
	}

	override val typ: Class<EssenceRecipeProvider.EssenceUpgradeRecipe>
		get() = EssenceRecipeProvider.EssenceUpgradeRecipe::class.java
}

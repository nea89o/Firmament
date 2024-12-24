package moe.nea.firmament.compat.rei.recipes

import java.util.Optional
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.client.view.ViewSearchBuilder
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.compat.rei.SBItemEntryDefinition
import moe.nea.firmament.repo.Reforge
import moe.nea.firmament.repo.ReforgeStore
import moe.nea.firmament.repo.RepoItemTypeCache
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.tr

class SBReforgeRecipe(
	val reforge: Reforge,
	val limitToItem: SkyblockId?,
) : Display {
	companion object {
		val catIdentifier = CategoryIdentifier.of<SBReforgeRecipe>(Firmament.MOD_ID, "reforge_recipe")
	}

	object Category : DisplayCategory<SBReforgeRecipe> {
		override fun getCategoryIdentifier(): CategoryIdentifier<out SBReforgeRecipe> {
			return catIdentifier
		}

		override fun getTitle(): Text {
			return tr("firmament.recipecategory.reforge", "Reforge")
		}

		override fun getIcon(): Renderer {
			return SBItemEntryDefinition.getEntry(SkyblockId("REFORGE_ANVIL"))
		}

		override fun setupDisplay(display: SBReforgeRecipe, bounds: Rectangle): MutableList<Widget> {
			val list = mutableListOf<Widget>()
			list.add(Widgets.createRecipeBase(bounds))
			// TODO: actual layout after christmas, probably
			list.add(Widgets.createSlot(Point(bounds.minX + 10, bounds.centerY))
				         .markInput().entries(display.inputItems))
			val stoneSlot = Widgets.createSlot(Point(bounds.minX + 38, bounds.centerY))
				.markInput()
			if (display.reforgeStone != null)
				stoneSlot.entry(display.reforgeStone)
			list.add(stoneSlot)
			list.add(Widgets.createSlot(Point(bounds.minX + 38 + 18, bounds.centerY))
				         .markInput().entries(display.outputItems))
			return list
		}
	}

	object DynamicGenerator : DynamicDisplayGenerator<SBReforgeRecipe> {
		fun getRecipesForSBItemStack(item: SBItemStack): Optional<List<SBReforgeRecipe>> {
			val reforgeRecipes = mutableListOf<SBReforgeRecipe>()
			for (reforge in ReforgeStore.findEligibleForInternalName(item.skyblockId)) {
				reforgeRecipes.add(SBReforgeRecipe(reforge, item.skyblockId))
			}
			for (reforge in ReforgeStore.findEligibleForItem(item.itemType ?: ItemType.NIL)) {
				reforgeRecipes.add(SBReforgeRecipe(reforge, item.skyblockId))
			}
			if (reforgeRecipes.isEmpty()) return Optional.empty()
			return Optional.of(reforgeRecipes)
		}

		override fun getRecipeFor(entry: EntryStack<*>): Optional<List<SBReforgeRecipe>> {
			if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
			val item = entry.castValue<SBItemStack>()
			return getRecipesForSBItemStack(item)
		}

		override fun getUsageFor(entry: EntryStack<*>): Optional<List<SBReforgeRecipe>> {
			if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
			val item = entry.castValue<SBItemStack>()
			ReforgeStore.byReforgeStone[item.skyblockId]?.let { stoneReforge ->
				return Optional.of(listOf(SBReforgeRecipe(stoneReforge, null)))
			}
			return getRecipesForSBItemStack(item)
		}

		override fun generate(builder: ViewSearchBuilder): Optional<List<SBReforgeRecipe>> {
			// TODO: check builder.recipesFor and such and optionally return all reforge recipes
			return Optional.empty()
		}
	}

	private val eligibleItems =
		if (limitToItem != null) listOfNotNull(RepoManager.getNEUItem(limitToItem))
		else reforge.eligibleItems.flatMap {
			when (it) {
				is Reforge.ReforgeEligibilityFilter.AllowsInternalName ->
					listOfNotNull(RepoManager.getNEUItem(it.internalName))
				is Reforge.ReforgeEligibilityFilter.AllowsItemType ->
					ReforgeStore.resolveItemType(it.itemType)
						.flatMap {
							RepoItemTypeCache.byItemType[it] ?: listOf()
						}

				is Reforge.ReforgeEligibilityFilter.AllowsVanillaItemType -> {
					listOf() // TODO: add filter support for this and potentially rework this to search for the declared item type in repo, instead of remapped item type
				}
			}
		}
	private val inputItems = eligibleItems.map { SBItemEntryDefinition.getEntry(it.skyblockId) }
	private val outputItems =
		inputItems.map { SBItemEntryDefinition.getEntry(it.value.copy(reforge = reforge.reforgeId)) }
	private val reforgeStone = reforge.reforgeStone?.let(SBItemEntryDefinition::getEntry)
	private val inputEntries =
		listOf(EntryIngredient.of(inputItems)) + listOfNotNull(reforgeStone?.let(EntryIngredient::of))
	private val outputEntries = listOf(EntryIngredient.of(outputItems))

	override fun getInputEntries(): List<EntryIngredient> {
		return inputEntries
	}

	override fun getOutputEntries(): List<EntryIngredient> {
		return outputEntries
	}

	override fun getCategoryIdentifier(): CategoryIdentifier<*> {
		return catIdentifier
	}

	override fun getDisplayLocation(): Optional<Identifier> {
		return Optional.empty()
	}

	override fun getSerializer(): DisplaySerializer<out Display>? {
		return null
	}
}

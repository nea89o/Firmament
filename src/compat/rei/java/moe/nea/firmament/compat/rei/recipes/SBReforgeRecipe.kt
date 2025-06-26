@file:OptIn(ExpensiveItemCacheApi::class)

package moe.nea.firmament.compat.rei.recipes

import java.util.Optional
import me.shedaniel.math.Dimension
import me.shedaniel.math.FloatingDimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Label
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
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.village.VillagerProfession
import moe.nea.firmament.Firmament
import moe.nea.firmament.compat.rei.EntityWidget
import moe.nea.firmament.compat.rei.SBItemEntryDefinition
import moe.nea.firmament.gui.entity.EntityRenderer
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.Reforge
import moe.nea.firmament.repo.ReforgeStore
import moe.nea.firmament.repo.RepoItemTypeCache
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.AprilFoolsUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.gold
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.tr

class SBReforgeRecipe(
	val reforge: Reforge,
	val limitToItem: SBItemStack?,
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
			return SBItemEntryDefinition.getEntry(SkyBlockItems.REFORGE_ANVIL)
		}

		override fun setupDisplay(display: SBReforgeRecipe, bounds: Rectangle): MutableList<Widget> {
			val list = mutableListOf<Widget>()
			list.add(Widgets.createRecipeBase(bounds))
			val inputSlot = Widgets.createSlot(Point(bounds.minX + 10, bounds.centerY - 9))
				.markInput().entries(display.inputItems)
			list.add(inputSlot)
			if (display.reforgeStone != null) {
				list.add(Widgets.createSlot(Point(bounds.minX + 10 + 24, bounds.centerY - 9 - 10))
					         .markInput().entry(display.reforgeStone))
				list.add(Widgets.withTooltip(
					Widgets.withTranslate(Widgets.wrapRenderer(
						Rectangle(Point(bounds.minX + 10 + 24, bounds.centerY - 9 + 10), Dimension(16, 16)),
						SBItemEntryDefinition.getEntry(SkyBlockItems.REFORGE_ANVIL)), 0.0, 0.0, 150.0),
					Rarity.entries.mapNotNull { rarity ->
						display.reforge.reforgeCosts?.get(rarity)?.let { rarity to it }
					}.map { (rarity, cost) ->
						Text.literal("")
							.append(rarity.text)
							.append(": ")
							.append(Text.literal("${FirmFormatters.formatCommas(cost, 0)} Coins").gold())
					}
				))
			} else {
				val size = if (AprilFoolsUtil.isAprilFoolsDay) 1.2 else 0.6
				val dimension =
					FloatingDimension(EntityWidget.defaultSize.width * size, EntityWidget.defaultSize.height * size)
				list.add(Widgets.withTooltip(
					EntityWidget(
						EntityType.VILLAGER.create(EntityRenderer.fakeWorld, SpawnReason.COMMAND)
							?.also { it.villagerData = it.villagerData.withProfession(MC.currentOrDefaultRegistries.getEntryOrThrow(VillagerProfession.WEAPONSMITH)) },
						Point(bounds.minX + 10 + 24 + 8 - dimension.width / 2, bounds.centerY - dimension.height / 2),
						dimension
					),
					tr("firmament.recipecategory.reforge.basic",
					   "This is a basic reforge, available at the Blacksmith.").grey()
				))
			}
			list.add(Widgets.createSlot(Point(bounds.minX + 10 + 24 + 24, bounds.centerY - 9))
				         .markInput().entries(display.outputItems))
			val statToLineMappings = mutableListOf<Pair<String, Label>>()
			for ((i, statId) in display.reforge.statUniverse.withIndex()) {
				val label = Widgets.createLabel(
					Point(bounds.minX + 10 + 24 + 24 + 20, bounds.minY + 8 + i * 11),
					SBItemStack.Companion.StatLine(SBItemStack.statIdToName(statId), null).reconstitute(7))
					.horizontalAlignment(Label.LEFT_ALIGNED)
				statToLineMappings.add(statId to label)
				list.add(label)
			}
			fun updateStatLines() {
				val entry = inputSlot.currentEntry?.castValue<SBItemStack>() ?: return
				val stats = display.reforge.reforgeStats?.get(entry.rarity) ?: mapOf()
				for ((stat, label) in statToLineMappings) {
					label.message =
						SBItemStack.Companion.StatLine(
							SBItemStack.statIdToName(stat), null,
							valueNum = stats[stat]
						).reconstitute(7)
				}
			}
			updateStatLines()
			inputSlot.withEntriesListener { updateStatLines() }
			return list
		}
	}

	object DynamicGenerator : DynamicDisplayGenerator<SBReforgeRecipe> {
		fun getRecipesForSBItemStack(item: SBItemStack): Optional<List<SBReforgeRecipe>> {
			val reforgeRecipes = mutableListOf<SBReforgeRecipe>()
			for (reforge in ReforgeStore.findEligibleForInternalName(item.skyblockId)) {
				reforgeRecipes.add(SBReforgeRecipe(reforge, item))
			}
			for (reforge in ReforgeStore.findEligibleForItem(item.itemType ?: ItemType.NIL)) {
				reforgeRecipes.add(SBReforgeRecipe(reforge, item))
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

	private val inputItems = run {
		if (limitToItem != null) return@run listOf(SBItemEntryDefinition.getEntry(limitToItem))
		val eligibleItems = reforge.eligibleItems.flatMap {
			when (it) {
					is Reforge.ReforgeEligibilityFilter.AllowsInternalName ->
						listOfNotNull(RepoManager.getNEUItem(it.internalName))

					is Reforge.ReforgeEligibilityFilter.AllowsItemType ->
						ReforgeStore.resolveItemType(it.itemType)
							.flatMapTo(mutableSetOf()) {
								(RepoItemTypeCache.byItemType[it] ?: listOf()) +
									(RepoItemTypeCache.byItemType[it.dungeonVariant] ?: listOf())
							}.toList()

					is Reforge.ReforgeEligibilityFilter.AllowsVanillaItemType -> {
						listOf() // TODO: add filter support for this and potentially rework this to search for the declared item type in repo, instead of remapped item type
					}
				}
		}
		eligibleItems.map { SBItemEntryDefinition.getEntry(it.skyblockId) }
	}
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

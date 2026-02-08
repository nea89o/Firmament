package moe.nea.firmament.repo.recipes

import io.github.moulberry.repo.NEURepository
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.npc.villager.VillagerProfession
import net.minecraft.world.item.ItemStack
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.entity.EntityRenderer
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.Reforge
import moe.nea.firmament.repo.ReforgeStore
import moe.nea.firmament.repo.RepoItemTypeCache
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.FirmFormatters.formatCommas
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.gold
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.tr

object SBReforgeRecipeRenderer : GenericRecipeRenderer<Reforge> {
	@OptIn(ExpensiveItemCacheApi::class)
	override fun render(
		recipe: Reforge,
		bounds: Rectangle,
		layouter: RecipeLayouter,
		mainItem: SBItemStack?
	) {
		val inputSlot = layouter.createCyclingItemSlot(
			bounds.minX + 10, bounds.centerY - 9,
			if (mainItem != null) listOf(mainItem)
			else generateAllItems(recipe),
			RecipeLayouter.SlotKind.SMALL_INPUT
		)
		val outputSlut = layouter.createItemSlot(
			bounds.minX + 10 + 24 + 24, bounds.centerY - 9,
			null,
			RecipeLayouter.SlotKind.SMALL_OUTPUT
		)
		val statLines = mutableListOf<Pair<String, RecipeLayouter.Updater<Component>>>()
		for ((i, statId) in recipe.statUniverse.withIndex()) {
			val label = layouter.createLabel(
				bounds.minX + 10 + 24 + 24 + 20, bounds.minY + 8 + i * 11,
				Component.empty()
			)
			statLines.add(statId to label)
		}

		fun updateOutput() {
			val currentBaseItem = inputSlot.current()
			outputSlut.update(currentBaseItem.copy(reforge = recipe.reforgeId))
			val stats = recipe.reforgeStats?.get(currentBaseItem.rarity) ?: mapOf()
			for ((stat, label) in statLines) {
				label.update(
					SBItemStack.Companion.StatLine(
						SBItemStack.statIdToName(stat), null,
						valueNum = stats[stat]
					).reconstitute(7)
				)
			}
		}

		if (recipe.reforgeStone != null) {
			layouter.createItemSlot(
				bounds.minX + 10 + 24, bounds.centerY - 9 - 10,
				SBItemStack(recipe.reforgeStone),
				RecipeLayouter.SlotKind.SMALL_INPUT
			)
			val d = Rectangle(
				bounds.minX + 10 + 24, bounds.centerY - 9 + 10,
				16, 16
			)
			layouter.createItemSlot(
				d.x, d.y,
				SBItemStack(SkyBlockItems.REFORGE_ANVIL),
				RecipeLayouter.SlotKind.DISPLAY
			)
			layouter.createTooltip(
				d,
				Rarity.entries.mapNotNull { rarity ->
					recipe.reforgeCosts?.get(rarity)?.let { rarity to it }
				}.map { (rarity, cost) ->
					Component.literal("")
						.append(rarity.text)
						.append(": ")
						.append(Component.literal("${formatCommas(cost, 0)} Coins").gold())
				}
			)
		} else {
			val entity = EntityType.VILLAGER.create(EntityRenderer.fakeWorld, EntitySpawnReason.COMMAND)
				?.also {
					it.villagerData =
						it.villagerData.withProfession(
							MC.currentOrDefaultRegistries,
							VillagerProfession.WEAPONSMITH
						)
				}
			val dim = EntityRenderer.defaultSize
			val d = Rectangle(
				Point(bounds.minX + 10 + 24 + 8 - dim.width / 2, bounds.centerY - dim.height / 2),
				dim
			)
			if (entity != null)
				layouter.createEntity(
					d,
					entity
				)
			layouter.createTooltip(
				d,
				tr(
					"firmament.recipecategory.reforge.basic",
					"This is a basic reforge, available at the Blacksmith."
				).grey()
			)
		}
	}

	private fun generateAllItems(recipe: Reforge): List<SBItemStack> {
		return recipe.eligibleItems.flatMap {
			when (it) {
				is Reforge.ReforgeEligibilityFilter.AllowsInternalName -> listOf(SBItemStack(it.internalName))
				is Reforge.ReforgeEligibilityFilter.AllowsItemType ->
					ReforgeStore.resolveItemType(it.itemType)
						.flatMapTo(mutableSetOf()) { itemType ->
							listOf(itemType, itemType.dungeonVariant)
						}
						.flatMapTo(mutableSetOf()) { itemType ->
							RepoItemTypeCache.byItemType[itemType] ?: listOf()
						}
						.map { SBItemStack(it.skyblockId) }

				is Reforge.ReforgeEligibilityFilter.AllowsVanillaItemType -> listOf()
			}
		}
	}

	override fun getInputs(recipe: Reforge): Collection<SBItemStack> {
		val reforgeStone = recipe.reforgeStone ?: return emptyList()
		return listOf(SBItemStack(reforgeStone))
	}

	override fun getOutputs(recipe: Reforge): Collection<SBItemStack> {
		return listOf()
	}

	@OptIn(ExpensiveItemCacheApi::class)
	override val icon: ItemStack
		get() = SBItemStack(SkyBlockItems.REFORGE_ANVIL).asImmutableItemStack()
	override val title: Component
		get() = tr("firmament.recipecategory.reforge", "Reforge")
	override val identifier: Identifier
		get() = Firmament.identifier("reforge_recipe")

	override fun findAllRecipes(neuRepository: NEURepository): Iterable<Reforge> {
		return ReforgeStore.allReforges
	}

	override val typ: Class<Reforge>
		get() = Reforge::class.java
}

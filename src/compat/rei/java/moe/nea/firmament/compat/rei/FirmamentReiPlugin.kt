package moe.nea.firmament.compat.rei

import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.resources.Identifier
import moe.nea.firmament.compat.rei.recipes.GenericREIRecipeCategory
import moe.nea.firmament.compat.rei.recipes.SBKatRecipe
import moe.nea.firmament.compat.rei.recipes.SBMobDropRecipe
import moe.nea.firmament.compat.rei.recipes.SBRecipe
import moe.nea.firmament.compat.rei.recipes.SBReforgeRecipe
import moe.nea.firmament.compat.rei.recipes.SBShopRecipe
import moe.nea.firmament.events.HandledScreenPushREIEvent
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.repo.recipes.SBCraftingRecipeRenderer
import moe.nea.firmament.repo.recipes.SBEssenceUpgradeRecipeRenderer
import moe.nea.firmament.repo.recipes.SBForgeRecipeRenderer
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.guessRecipeId
import moe.nea.firmament.util.IntUtil.romanToInt
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblock.Rarity


class FirmamentReiPlugin : REIClientPlugin {

	companion object {
		@ExpensiveItemCacheApi
		fun EntryStack<SBItemStack>.asItemEntry(): EntryStack<ItemStack> {
			return EntryStack.of(VanillaEntryTypes.ITEM, value.asImmutableItemStack())
		}

		val SKYBLOCK_ITEM_TYPE_ID = Identifier.fromNamespaceAndPath("firmament", "skyblockitems")
	}

	@OptIn(ExpensiveItemCacheApi::class)
	override fun registerTransferHandlers(registry: TransferHandlerRegistry) {
		if (!RepoManager.shouldLoadREI()) return
		registry.register(TransferHandler { context ->
			val screen = context.containerScreen
			val display = context.display
			if (display !is SBRecipe) return@TransferHandler TransferHandler.Result.createNotApplicable()
			val recipe = display.neuRecipe
			if (recipe !is NEUCraftingRecipe) return@TransferHandler TransferHandler.Result.createNotApplicable()
			val neuItem = RepoManager.getNEUItem(SkyblockId(recipe.output.itemId))
				?: error("Could not find neu item ${recipe.output.itemId} which is used in a recipe output")
			val useSuperCraft = context.isStackedCrafting || RepoManager.TConfig.alwaysSuperCraft
			if (neuItem.isVanilla && useSuperCraft) return@TransferHandler TransferHandler.Result.createFailed(
				Component.translatable(
					"firmament.recipe.novanilla"
				)
			)
			var shouldReturn = true
			if (context.isActuallyCrafting && !useSuperCraft) {
				val craftingScreen = (screen as? ContainerScreen)
					?.takeIf { it.title.string == CraftingOverlay.CRAFTING_SCREEN_NAME }
				if (craftingScreen == null) {
					MC.sendCommand("craft")
					shouldReturn = false
				}
				CraftingOverlay.setOverlay(craftingScreen, recipe)
			}
			if (context.isActuallyCrafting && useSuperCraft) {
				shouldReturn = false
				MC.sendCommand("viewrecipe ${neuItem.guessRecipeId()}")
			}
			return@TransferHandler TransferHandler.Result.createSuccessful().blocksFurtherHandling(shouldReturn)
		})
	}


	val generics = listOf<GenericREIRecipeCategory<*>>(
		// Order matters: The order in here is the order in which they show up in REI
		GenericREIRecipeCategory(SBCraftingRecipeRenderer),
		GenericREIRecipeCategory(SBForgeRecipeRenderer),
		GenericREIRecipeCategory(SBEssenceUpgradeRecipeRenderer),
	)

	override fun registerCategories(registry: CategoryRegistry) {
		if (!RepoManager.shouldLoadREI()) return

		registry.add(generics)
		registry.add(SBMobDropRecipe.Category)
		registry.add(SBKatRecipe.Category)
		registry.add(SBReforgeRecipe.Category)
		registry.add(SBShopRecipe.Category)
	}

	override fun registerExclusionZones(zones: ExclusionZones) {
		zones.register(AbstractContainerScreen::class.java) {
			HandledScreenPushREIEvent.publish(
				HandledScreenPushREIEvent(it)
			).rectangles
		}
		zones.register(StorageOverlayScreen::class.java) { it.getBounds() }
	}

	override fun registerDisplays(registry: DisplayRegistry) {
		if (!RepoManager.shouldLoadREI()) return

		generics.forEach {
			it.registerDynamicGenerator(registry)
		}
		registry.registerDisplayGenerator(
			SBReforgeRecipe.catIdentifier,
			SBReforgeRecipe.DynamicGenerator
		)
		registry.registerDisplayGenerator(
			SBMobDropRecipe.Category.categoryIdentifier,
			SkyblockMobDropRecipeDynamicGenerator
		)
		registry.registerDisplayGenerator(
			SBShopRecipe.Category.categoryIdentifier,
			SkyblockShopRecipeDynamicGenerator
		)
		registry.registerDisplayGenerator(
			SBKatRecipe.Category.categoryIdentifier,
			SkyblockKatRecipeDynamicGenerator
		)
	}

	override fun registerCollapsibleEntries(registry: CollapsibleEntryRegistry) {
		if (!RepoManager.shouldLoadREI()) return

		if (!RepoManager.TConfig.disableItemGroups)
			RepoManager.neuRepo.constants.parents.parents
				.forEach { (parent, children) ->
					registry.group(
						SkyblockId(parent).identifier,
						Component.literal(RepoManager.getNEUItem(SkyblockId(parent))?.displayName ?: parent),
						(children + parent).map { SBItemEntryDefinition.getEntry(SkyblockId(it)) })
				}
	}

	override fun registerScreens(registry: ScreenRegistry) {
		registry.registerDecider(object : OverlayDecider {
			override fun <R : Screen?> isHandingScreen(screen: Class<R>?): Boolean {
				return screen == StorageOverlayScreen::class.java
			}

			override fun <R : Screen?> shouldScreenBeOverlaid(screen: R): InteractionResult {
				return InteractionResult.SUCCESS
			}
		})
		registry.registerFocusedStack(SkyblockItemIdFocusedStackProvider)
	}

	/**
	 * Parses item display name into base name and tier for proper sorting
	 * Example: "Sharpness VI" -> ItemNameParts("Sharpness", 6)
	 */
	data class BaseNameAndTier(val baseName: String, val tier: Int)

	private enum class PrimaryGroup {
		TYPED_EXPLICIT, TYPED_OTHER, NAME_BASED_TYPE, LORE_BASED_WITH_RARITY, LORE_BASED_WITHOUT_RARITY
	}

	private data class SortableItem(
		val neuItem: NEUItem,
		val baseNameAndTier: BaseNameAndTier,
		val effectiveType: String,
		val primaryGroup: PrimaryGroup,
	)

	private val minionPattern = Regex("""^.+ Minion [IVXLCDM]+$""")
	private val potionPattern = Regex("""^.+ [IVXLCDM]+ Potion$""")
	private val splashPotionPattern = Regex("""^.+ [IVXLCDM]+ Splash Potion$""")
	private val romanNamePattern = """^(.+?)\s+([IVXLCDM]+)$""".toRegex()

	// Explicit type ordering; types not listed sort alphabetically after these.
	// Within each group, normal variants come before their DUNGEON counterparts.
	private val typeOrder = listOf(
		// -- Item Types --
		// Armor
		"HELMET", "DUNGEON HELMET",
		"CHESTPLATE", "DUNGEON CHESTPLATE",
		"LEGGINGS", "DUNGEON LEGGINGS",
		"BOOTS", "DUNGEON BOOTS",
		// Equipment
		"NECKLACE", "DUNGEON NECKLACE",
		"CLOAK", "DUNGEON CLOAK",
		"BELT", "DUNGEON BELT",
		"GLOVES", "DUNGEON GLOVES",
		"BRACELET", "DUNGEON BRACELET",
		// Weapons
		"SWORD", "DUNGEON SWORD", "LONGSWORD", "DUNGEON LONGSWORD",
		"BOW", "DUNGEON BOW", "ARROW",
		// Tools
		"PICKAXE", "DUNGEON PICKAXE",
		"DRILL", "AXE", "SHOVEL", "SHEARS", "FISHING ROD",
		"DEPLOYABLE", "LASSO", "FISHING NET", "TRAP", 
        // Pets
        "PET",
		// Accessories
		"ACCESSORY", "DUNGEON ACCESSORY",
		"HATCESSORY",

		// -- Name Based Types --
		"ENCHANTED BOOK", "MINION", "POTION", "SPLASH POTION", "SACK",
	)
	private val typeOrderIndex: Map<String, Int> = typeOrder.withIndex().associate { (i, t) -> t to i }

	private fun NEUItem.strippedDisplayName() = displayName.removeColorCodes()

	// Dark grey (§8) lore lines that act as a quasi item type (always title-cased in game).
	private val darkGreyQuasiTypes = setOf(
		"Brewing Ingredient",
		"Storage",
		"Collection Item",
		"Holiday Cosmetic",
		"Halloween Cosmetic",
		"Summer Cosmetic",
		"Easter Cosmetic",
		"Decoration Item",
		"Drill Part",
		"Basic Brew",
		"Quest Item",
	)

	/** Returns a quasi item type from dark grey lore text, or null if none matched. */
	private fun getQuasiItemType(neuItem: NEUItem): String? {
		for (i in 0 until minOf(4, neuItem.lore.size)) { // Only expected on first 4 lines
			val line = neuItem.lore[i]
			if (!line.startsWith("§8")) continue
			val plain = line.removeColorCodes().trim()
			if (plain in darkGreyQuasiTypes) return plain.uppercase()
		}
		return null
	}

	/** Resolves the effective display type and primary group for an item. */
	@OptIn(ExpensiveItemCacheApi::class)
	private fun toSortableItem(neuItem: NEUItem): SortableItem {
		val displayName = neuItem.strippedDisplayName()
		val itemType = with(ItemCache) { ItemType.fromItemStack(neuItem.asItemStack()) }
		val namedType = when {
			minionPattern.matches(displayName) -> "MINION"
			potionPattern.matches(displayName) -> "POTION"
			splashPotionPattern.matches(displayName) -> "SPLASH POTION"
			displayName.endsWith("Sack") -> "SACK"
			displayName == "Enchanted Book" -> "ENCHANTED BOOK"
			else -> null
		}
		val effectiveType = itemType?.toString()
			?: namedType
			?: getQuasiItemType(neuItem)
			?: "ZZZ"
		val primaryGroup = when {
			itemType != null && effectiveType in typeOrderIndex -> PrimaryGroup.TYPED_EXPLICIT
			itemType != null -> PrimaryGroup.TYPED_OTHER
			namedType != null -> PrimaryGroup.NAME_BASED_TYPE
			Rarity.fromEscapeCodeLore(neuItem.lore) != null -> PrimaryGroup.LORE_BASED_WITH_RARITY
			else -> PrimaryGroup.LORE_BASED_WITHOUT_RARITY
		}
		return SortableItem(neuItem, parseItemNameForSorting(neuItem), effectiveType, primaryGroup)
	}

	private fun parseItemNameForSorting(neuItem: NEUItem): BaseNameAndTier {
		val displayName = neuItem.strippedDisplayName()

		// For enchanted books the display name is always "Enchanted Book"; the actual
		// enchant name + level (e.g. "Sharpness V") is in the first lore line.
		val nameToParse = if (displayName == "Enchanted Book" && neuItem.lore.isNotEmpty()) {
			neuItem.lore[0].removeColorCodes().trim()
		} else {
			displayName
		}

		// Match pattern like "Sharpness VI" or "Snow Minion XII"
		val match = romanNamePattern.find(nameToParse)

		return if (match != null) {
			val (baseName, romanNumeral) = match.destructured
			val tier = romanNumeral.romanToInt()
			BaseNameAndTier(baseName, tier)
		} else {
			// No tier fallback - use 0 so it sorts before tiered versions
			BaseNameAndTier(nameToParse, 0)
		}
	}

	@OptIn(ExpensiveItemCacheApi::class)
	override fun registerEntries(registry: EntryRegistry) {
		if (!RepoManager.shouldLoadREI()) return

		registry.removeEntryIf { true }

		// SORTING ORDER:
		// ┌─ [1] Primary Group:
		// │   ├─ TYPED_EXPLICIT            — fromItemStack returned a known type in typeOrderIndex
		// │   ├─ TYPED_OTHER               — fromItemStack returned a type not in typeOrderIndex
		// │   ├─ NAME_BASED_TYPE           — display-name detected: MINION, POTION, SACK, ENCHANTED BOOK
		// │   ├─ LORE_BASED_WITH_RARITY 	— dark-grey quasi type (eg. "Decoration Item") and item has a rarity line
		// │   └─ LORE_BASED_WITHOUT_RARITY — dark-grey quasi type, no rarity line
		// ├─ [2] Item Type:
		// │   ├─ TYPED_EXPLICIT: Armor:     HELMET → DUNGEON HELMET → CHESTPLATE → DUNGEON CHESTPLATE
		// │   │                             LEGGINGS → DUNGEON LEGGINGS → BOOTS → DUNGEON BOOTS
		// │   │                  Equipment: NECKLACE → DUNGEON NECKLACE → CLOAK → DUNGEON CLOAK
		// │   │                             BELT → DUNGEON BELT → GLOVES → DUNGEON GLOVES
		// │   │                             BRACELET → DUNGEON BRACELET
		// │   │                  Weapons:   SWORD → DUNGEON SWORD → LONGSWORD → DUNGEON LONGSWORD
		// │   │                             BOW → DUNGEON BOW → ARROW
		// │   │                  Tools:     PICKAXE → DUNGEON PICKAXE → DRILL → AXE → SHOVEL → SHEARS → FISHING ROD
		// │   │                             DEPLOYABLE → LASSO → FISHING NET → TRAP → PET
		// │   │                  Accessory: ACCESSORY → DUNGEON ACCESSORY → HATCESSORY
		// │   └─ All other groups: alphabetical
		// ├─ [3] Name (alphabetical)
		// └─ [4] Tier (I=1, II=2, ...)
		RepoManager.neuRepo.items?.items?.values
			?.map { toSortableItem(it) }
			?.sortedWith(
				compareBy(
				{ it.primaryGroup },
				{ typeOrderIndex[it.effectiveType] ?: Int.MAX_VALUE },
				{ it.effectiveType },
				{ it.baseNameAndTier.baseName },
				{ it.baseNameAndTier.tier }
			))
			?.forEach {
				registry.addEntry(SBItemEntryDefinition.getEntry(it.neuItem.skyblockId))
			}
	}
}

package moe.nea.firmament.compat.rei

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
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import moe.nea.firmament.compat.rei.recipes.SBCraftingRecipe
import moe.nea.firmament.compat.rei.recipes.SBEssenceUpgradeRecipe
import moe.nea.firmament.compat.rei.recipes.SBForgeRecipe
import moe.nea.firmament.compat.rei.recipes.SBKatRecipe
import moe.nea.firmament.compat.rei.recipes.SBMobDropRecipe
import moe.nea.firmament.compat.rei.recipes.SBReforgeRecipe
import moe.nea.firmament.compat.rei.recipes.SBShopRecipe
import moe.nea.firmament.events.HandledScreenPushREIEvent
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.guessRecipeId
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.unformattedString


class FirmamentReiPlugin : REIClientPlugin {

	companion object {
		fun EntryStack<SBItemStack>.asItemEntry(): EntryStack<ItemStack> {
			return EntryStack.of(VanillaEntryTypes.ITEM, value.asImmutableItemStack())
		}

		val SKYBLOCK_ITEM_TYPE_ID = Identifier.of("firmament", "skyblockitems")
	}

	override fun registerTransferHandlers(registry: TransferHandlerRegistry) {
		registry.register(TransferHandler { context ->
			val screen = context.containerScreen
			val display = context.display
			if (display !is SBCraftingRecipe) return@TransferHandler TransferHandler.Result.createNotApplicable()
			val neuItem = RepoManager.getNEUItem(SkyblockId(display.neuRecipe.output.itemId))
				?: error("Could not find neu item ${display.neuRecipe.output.itemId} which is used in a recipe output")
			val useSuperCraft = context.isStackedCrafting || RepoManager.Config.alwaysSuperCraft
			if (neuItem.isVanilla && useSuperCraft) return@TransferHandler TransferHandler.Result.createFailed(Text.translatable(
				"firmament.recipe.novanilla"))
			var shouldReturn = true
			if (context.isActuallyCrafting && !useSuperCraft) {
				if (screen !is GenericContainerScreen || screen.title?.unformattedString != CraftingOverlay.CRAFTING_SCREEN_NAME) {
					MC.sendCommand("craft")
					shouldReturn = false
				}
				CraftingOverlay.setOverlay(screen as? GenericContainerScreen, display.neuRecipe)
			}
			if (context.isActuallyCrafting && useSuperCraft) {
				shouldReturn = false
				MC.sendCommand("viewrecipe ${neuItem.guessRecipeId()}")
			}
			return@TransferHandler TransferHandler.Result.createSuccessful().blocksFurtherHandling(shouldReturn)
		})
	}


	override fun registerCategories(registry: CategoryRegistry) {
		registry.add(SBCraftingRecipe.Category)
		registry.add(SBForgeRecipe.Category)
		registry.add(SBMobDropRecipe.Category)
		registry.add(SBKatRecipe.Category)
		registry.add(SBReforgeRecipe.Category)
		registry.add(SBEssenceUpgradeRecipe.Category)
		registry.add(SBShopRecipe.Category)
	}

	override fun registerExclusionZones(zones: ExclusionZones) {
		zones.register(HandledScreen::class.java) { HandledScreenPushREIEvent.publish(HandledScreenPushREIEvent(it)).rectangles }
		zones.register(StorageOverlayScreen::class.java) { it.getBounds() }
	}

	override fun registerDisplays(registry: DisplayRegistry) {
		registry.registerDisplayGenerator(
			SBCraftingRecipe.Category.catIdentifier,
			SkyblockCraftingRecipeDynamicGenerator)
		registry.registerDisplayGenerator(
			SBReforgeRecipe.catIdentifier,
			SBReforgeRecipe.DynamicGenerator
		)
		registry.registerDisplayGenerator(
			SBForgeRecipe.Category.categoryIdentifier,
			SkyblockForgeRecipeDynamicGenerator)
		registry.registerDisplayGenerator(
			SBMobDropRecipe.Category.categoryIdentifier,
			SkyblockMobDropRecipeDynamicGenerator)
		registry.registerDisplayGenerator(
			SBShopRecipe.Category.categoryIdentifier,
			SkyblockShopRecipeDynamicGenerator)
		registry.registerDisplayGenerator(
			SBKatRecipe.Category.categoryIdentifier,
			SkyblockKatRecipeDynamicGenerator)
		registry.registerDisplayGenerator(
			SBEssenceUpgradeRecipe.Category.categoryIdentifier,
			SkyblockEssenceRecipeDynamicGenerator
		)
	}

	override fun registerCollapsibleEntries(registry: CollapsibleEntryRegistry) {
		if (!RepoManager.Config.disableItemGroups)
			RepoManager.neuRepo.constants.parents.parents
				.forEach { (parent, children) ->
					registry.group(
						SkyblockId(parent).identifier,
						Text.literal(RepoManager.getNEUItem(SkyblockId(parent))?.displayName ?: parent),
						(children + parent).map { SBItemEntryDefinition.getEntry(SkyblockId(it)) })
				}
	}

	override fun registerScreens(registry: ScreenRegistry) {
		registry.registerDecider(object : OverlayDecider {
			override fun <R : Screen?> isHandingScreen(screen: Class<R>?): Boolean {
				return screen == StorageOverlayScreen::class.java
			}

			override fun <R : Screen?> shouldScreenBeOverlaid(screen: R): ActionResult {
				return ActionResult.SUCCESS
			}
		})
		registry.registerFocusedStack(SkyblockItemIdFocusedStackProvider)
	}

	override fun registerEntries(registry: EntryRegistry) {
		registry.removeEntryIf { true }
		RepoManager.neuRepo.items?.items?.values?.forEach { neuItem ->
			registry.addEntry(SBItemEntryDefinition.getEntry(neuItem.skyblockId))
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import moe.nea.firmament.events.HandledScreenPushREIEvent
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.rei.recipes.SBCraftingRecipe
import moe.nea.firmament.rei.recipes.SBForgeRecipe
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.unformattedString
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.rei.recipes.SBMobDropRecipe


class FirmamentReiPlugin : REIClientPlugin {

    companion object {
        fun EntryStack<SBItemStack>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value.asImmutableItemStack())
        }

        val SKYBLOCK_ITEM_TYPE_ID = Identifier("firmament", "skyblockitems")
    }
    override fun registerTransferHandlers(registry: TransferHandlerRegistry) {
        registry.register(TransferHandler { context ->
            val screen = context.containerScreen
            val display = context.display
            if (display !is SBCraftingRecipe || screen !is GenericContainerScreen || screen.title?.unformattedString != "Craft Item") {
                return@TransferHandler TransferHandler.Result.createNotApplicable()
            }
            if (context.isActuallyCrafting)
                CraftingOverlay.setOverlay(screen, display)
            return@TransferHandler TransferHandler.Result.createSuccessful().blocksFurtherHandling(true)
        })
    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
    }

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(SBCraftingRecipe.Category)
        registry.add(SBForgeRecipe.Category)
        registry.add(SBMobDropRecipe.Category)
    }

    override fun registerExclusionZones(zones: ExclusionZones) {
        zones.register(HandledScreen::class.java) { HandledScreenPushREIEvent.publish(HandledScreenPushREIEvent(it)).rectangles }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.registerDisplayGenerator(
            SBCraftingRecipe.Category.catIdentifier,
            SkyblockCraftingRecipeDynamicGenerator
        )
        registry.registerDisplayGenerator(
            SBForgeRecipe.Category.categoryIdentifier,
            SkyblockForgeRecipeDynamicGenerator
        )
        registry.registerDisplayGenerator(SBMobDropRecipe.Category.categoryIdentifier, SkyblockMobDropRecipeDynamicGenerator)
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
        registry.registerFocusedStack(SkyblockItemIdFocusedStackProvider)
    }

    override fun registerEntries(registry: EntryRegistry) {
        registry.removeEntryIf { true }
        RepoManager.neuRepo.items?.items?.values?.forEach { neuItem ->
            registry.addEntry(SBItemEntryDefinition.getEntry(neuItem.skyblockId))
        }
    }
}

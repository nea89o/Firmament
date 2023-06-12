/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.rei

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.recipes.SBCraftingRecipe
import moe.nea.firmament.recipes.SBForgeRecipe
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.unformattedString


class FirmamentReiPlugin : REIClientPlugin {

    companion object {
        fun EntryStack<SBItemStack>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value.asItemStack())
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
            return@TransferHandler TransferHandler.Result.createSuccessful()
        })
    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
    }

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(SBCraftingRecipe.Category)
        registry.add(SBForgeRecipe.Category)
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
    }

    override fun registerCollapsibleEntries(registry: CollapsibleEntryRegistry) {
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

/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei.recipes

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.rei.SBItemStack
import moe.nea.firmament.repo.EssenceRecipeProvider
import moe.nea.firmament.util.SkyblockId

class SBEssenceUpgradeRecipe(override val neuRecipe: EssenceRecipeProvider.EssenceUpgradeRecipe) : SBRecipe() {
    object Category : DisplayCategory<SBEssenceUpgradeRecipe> {
        override fun getCategoryIdentifier(): CategoryIdentifier<SBEssenceUpgradeRecipe> =
            CategoryIdentifier.of(Firmament.MOD_ID, "essence_upgrade")

        override fun getTitle(): Text {
            return Text.literal("Essence Upgrades")
        }

        override fun getIcon(): Renderer {
            return SBItemEntryDefinition.getEntry(SkyblockId("ESSENCE_WITHER"))
        }

        override fun setupDisplay(display: SBEssenceUpgradeRecipe, bounds: Rectangle): List<Widget> {
            val recipe = display.neuRecipe
            val list = mutableListOf<Widget>()
            list.add(Widgets.createRecipeBase(bounds))
            list.add(Widgets.createSlot(Point(bounds.minX + 12, bounds.centerY - 8 - 18 / 2))
                         .markInput()
                         .entry(SBItemEntryDefinition.getEntry(SBItemStack(recipe.itemId).copy(stars = recipe.starCountAfter - 1))))
            list.add(Widgets.createSlot(Point(bounds.minX + 12, bounds.centerY - 8 + 18 / 2))
                         .markInput()
                         .entry(SBItemEntryDefinition.getEntry(recipe.essenceIngredient)))
            list.add(Widgets.createSlot(Point(bounds.maxX - 12 - 16, bounds.centerY - 8))
                         .markOutput()
                         .entry(SBItemEntryDefinition.getEntry(SBItemStack(recipe.itemId).copy(stars = recipe.starCountAfter))))
            val extraItems = recipe.extraItems
            list.add(Widgets.createArrow(Point(bounds.centerX - 24 / 2,
                                               if (extraItems.isEmpty()) bounds.centerY - 17 / 2
                                               else bounds.centerY + 18 / 2)))
            for ((index, item) in extraItems.withIndex()) {
                list.add(Widgets.createSlot(
                    Point(bounds.centerX - extraItems.size * 16 / 2 - 2 / 2 + index * 18,
                          bounds.centerY - 18 / 2))
                             .markInput()
                             .entry(SBItemEntryDefinition.getEntry(item)))
            }
            return list
        }
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<*> {
        return Category.categoryIdentifier
    }
}

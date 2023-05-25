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

package moe.nea.firmament.recipes

import io.github.moulberry.repo.data.NEUForgeRecipe
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.block.Blocks
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.rei.SBItemEntryDefinition

class SBForgeRecipe(override val neuRecipe: NEUForgeRecipe) : SBRecipe() {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.categoryIdentifier

    object Category : DisplayCategory<SBForgeRecipe> {
        override fun getCategoryIdentifier(): CategoryIdentifier<SBForgeRecipe> =
            CategoryIdentifier.of(Firmament.MOD_ID, "forge_recipe")

        override fun getTitle(): Text = Text.literal("Forge Recipes")
        override fun getDisplayHeight(): Int {
            return super.getDisplayHeight()
        }

        override fun getIcon(): Renderer = EntryStacks.of(Blocks.ANVIL)
        override fun setupDisplay(display: SBForgeRecipe, bounds: Rectangle): List<Widget> {
            return buildList {
                // TODO: proper gui for this (possibly inspired by the old circular gui)
                add(Widgets.createRecipeBase(bounds))
                val resultSlot = Point(bounds.centerX, bounds.centerY + 5)
                add(Widgets.createResultSlotBackground(resultSlot))
                val ingredientsCenter = Point(bounds.centerX, bounds.centerY - 20)
                val count = display.neuRecipe.inputs.size
                display.neuRecipe.inputs.forEachIndexed { idx, ingredient ->
                    add(
                        Widgets.createSlot(
                            Point(ingredientsCenter.x + 12 - count * 24 / 2 + idx * 24, ingredientsCenter.y)
                        ).markInput().entry(SBItemEntryDefinition.getEntry(ingredient))
                    )
                }
                add(
                    Widgets.createSlot(resultSlot).markOutput().disableBackground()
                        .entry(SBItemEntryDefinition.getEntry(display.neuRecipe.outputStack))
                )
            }
        }
    }

}

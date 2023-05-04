package moe.nea.notenoughupdates.recipes

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
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.rei.SBItemEntryDefinition

class SBForgeRecipe(override val neuRecipe: NEUForgeRecipe) : SBRecipe() {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.categoryIdentifier

    object Category : DisplayCategory<SBForgeRecipe> {
        override fun getCategoryIdentifier(): CategoryIdentifier<SBForgeRecipe> =
            CategoryIdentifier.of(NotEnoughUpdates.MOD_ID, "forge_recipe")

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

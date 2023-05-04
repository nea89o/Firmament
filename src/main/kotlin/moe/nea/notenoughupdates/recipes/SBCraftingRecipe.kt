package moe.nea.notenoughupdates.recipes

import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUIngredient
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

class SBCraftingRecipe(override val neuRecipe: NEUCraftingRecipe) : SBRecipe() {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.catIdentifier

    object Category : DisplayCategory<SBCraftingRecipe> {
        val catIdentifier = CategoryIdentifier.of<SBCraftingRecipe>(NotEnoughUpdates.MOD_ID, "crafing_recipe")
        override fun getCategoryIdentifier(): CategoryIdentifier<out SBCraftingRecipe> = catIdentifier

        override fun getTitle(): Text = Text.literal("SkyBlock Crafting")

        override fun getIcon(): Renderer = EntryStacks.of(Blocks.CRAFTING_TABLE)
        override fun setupDisplay(display: SBCraftingRecipe, bounds: Rectangle): List<Widget> {
            val point = Point(bounds.centerX - 58, bounds.centerY - 27)
            return buildList {
                add(Widgets.createRecipeBase(bounds))
                add(Widgets.createArrow(Point(point.x + 60, point.y + 18)))
                add(Widgets.createResultSlotBackground(Point(point.x + 95, point.y + 19)))
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        val slot = Widgets.createSlot(Point(point.x + 1 + i * 18, point.y + 1 + j * 18)).markInput()
                        add(slot)
                        val item = display.neuRecipe.inputs[i + j * 3]
                        if (item == NEUIngredient.SENTINEL_EMPTY) continue
                        slot.entry(SBItemEntryDefinition.getEntry(item)) // TODO: make use of stackable item entries
                    }
                }
                add(
                    Widgets.createSlot(Point(point.x + 95, point.y + 19))
                        .entry(SBItemEntryDefinition.getEntry(display.neuRecipe.output))
                        .disableBackground().markOutput()
                )
            }
        }

    }

}

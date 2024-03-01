/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei.recipes

import io.github.moulberry.repo.data.NEUMobDropRecipe
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
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.entity.EntityRenderer
import moe.nea.firmament.gui.entity.EntityWidget
import moe.nea.firmament.rei.SBItemEntryDefinition

class SBMobDropRecipe(override val neuRecipe: NEUMobDropRecipe) : SBRecipe() {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.categoryIdentifier

    object Category : DisplayCategory<SBMobDropRecipe> {
        override fun getCategoryIdentifier(): CategoryIdentifier<SBMobDropRecipe> =
            CategoryIdentifier.of(Firmament.MOD_ID, "mob_drop_recipe")

        override fun getTitle(): Text = Text.literal("Mob Drops")
        override fun getDisplayHeight(): Int {
            return 100
        }

        override fun getIcon(): Renderer = EntryStacks.of(Blocks.ANVIL)
        override fun setupDisplay(display: SBMobDropRecipe, bounds: Rectangle): List<Widget> {
            return buildList {
                add(Widgets.createRecipeBase(bounds))
                val source = display.neuRecipe.render
                val entity = if (source.startsWith("@")) {
                    EntityRenderer.constructEntity(Identifier(source.substring(1)))
                } else {
                    EntityRenderer.applyModifiers(source, listOf())
                }
                if (entity != null) {
                    val level = display.neuRecipe.level
                    val fullMobName =
                        if (level > 0) Text.translatable("firmament.recipe.mobs.name", level, display.neuRecipe.name)
                        else Text.translatable("firmament.recipe.mobs.name.nolevel", display.neuRecipe.name)
                    val tt = mutableListOf<Text>()
                    tt.add((fullMobName))
                    tt.add(Text.literal(""))
                    if (display.neuRecipe.coins > 0) {
                        tt.add(Text.stringifiedTranslatable("firmament.recipe.mobs.coins", display.neuRecipe.coins))
                    }
                    if (display.neuRecipe.combatExperience > 0) {
                        tt.add(
                            Text.stringifiedTranslatable(
                                "firmament.recipe.mobs.combat",
                                display.neuRecipe.combatExperience
                            )
                        )
                    }
                    if (display.neuRecipe.enchantingExperience > 0) {
                        tt.add(
                            Text.stringifiedTranslatable(
                                "firmament.recipe.mobs.exp",
                                display.neuRecipe.enchantingExperience
                            )
                        )
                    }
                    if (display.neuRecipe.extra != null)
                        display.neuRecipe.extra.mapTo(tt) { Text.literal(it) }
                    if (tt.size == 2)
                        tt.removeAt(1)
                    add(
                        Widgets.withTooltip(
                            EntityWidget(entity, Point(bounds.minX + 5, bounds.minY + 15)),
                            tt
                        )
                    )
                }
                add(
                    Widgets.createLabel(Point(bounds.minX + 15, bounds.minY + 5), Text.literal(display.neuRecipe.name))
                        .leftAligned()
                )
                var x = bounds.minX + 60
                var y = bounds.minY + 20
                for (drop in display.neuRecipe.drops) {
                    val lore = drop.extra.mapTo(mutableListOf()) { Text.literal(it) }
                    if (drop.chance != null) {
                        lore += listOf(Text.translatable("firmament.recipe.mobs.drops", drop.chance))
                    }
                    val item = SBItemEntryDefinition.getEntry(drop.dropItem)
                        .value.copy(extraLore = lore)
                    add(
                        Widgets.createSlot(Point(x, y)).markOutput()
                            .entries(listOf(SBItemEntryDefinition.getEntry(item)))
                    )
                    x += 18
                    if (x > bounds.maxX - 30) {
                        x = bounds.minX + 60
                        y += 18
                    }
                }
            }
        }
    }

}

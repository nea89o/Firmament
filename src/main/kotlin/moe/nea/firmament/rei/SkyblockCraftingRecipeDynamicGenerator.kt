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

import io.github.moulberry.repo.data.NEUCraftingRecipe
import io.github.moulberry.repo.data.NEUForgeRecipe
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe
import java.util.*
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.client.view.ViewSearchBuilder
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryStack
import moe.nea.firmament.recipes.SBCraftingRecipe
import moe.nea.firmament.recipes.SBForgeRecipe
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.skyblockId


val SkyblockCraftingRecipeDynamicGenerator = neuDisplayGenerator<SBCraftingRecipe, NEUCraftingRecipe> {
    SBCraftingRecipe(it)
}

val SkyblockForgeRecipeDynamicGenerator = neuDisplayGenerator<SBForgeRecipe, NEUForgeRecipe> {
    SBForgeRecipe(it)
}

inline fun <D : Display, reified T : NEURecipe> neuDisplayGenerator(noinline mapper: (T) -> D) =
    object : DynamicDisplayGenerator<D> {
        override fun getRecipeFor(entry: EntryStack<*>): Optional<List<D>> {
            if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
            val item = entry.castValue<SBItemStack>()
            val recipes = RepoManager.getRecipesFor(item.skyblockId)
            val craftingRecipes = recipes.filterIsInstance<T>()
            return Optional.of(craftingRecipes.map(mapper))
        }

        override fun generate(builder: ViewSearchBuilder): Optional<List<D>> {
            if (SBCraftingRecipe.Category.catIdentifier !in builder.categories) return Optional.empty()
            return Optional.of(
                RepoManager.getAllRecipes().filterIsInstance<T>().map(mapper)
                    .toList()
            )
        }

        override fun getUsageFor(entry: EntryStack<*>): Optional<List<D>> {
            if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
            val item = entry.castValue<SBItemStack>()
            val recipes = RepoManager.getUsagesFor(item.skyblockId)
            val craftingRecipes = recipes.filterIsInstance<T>()
            return Optional.of(craftingRecipes.map(mapper))

        }
    }

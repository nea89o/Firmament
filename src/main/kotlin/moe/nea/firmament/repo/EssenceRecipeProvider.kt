/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEURecipe
import moe.nea.firmament.util.SkyblockId

class EssenceRecipeProvider : IReloadable {
    data class EssenceUpgradeRecipe(
        val itemId: SkyblockId,
        val starCountAfter: Int,
        val essenceCost: Int,
        val essenceType: String, // TODO: replace with proper type
        val extraItems: List<NEUIngredient>,
    ) : NEURecipe {
        val essenceIngredient= NEUIngredient.fromString("${essenceType}:$essenceCost")
        val allUpgradeComponents = listOf(essenceIngredient) + extraItems

        override fun getAllInputs(): Collection<NEUIngredient> {
            return listOf(NEUIngredient.fromString(itemId.neuItem + ":1")) + allUpgradeComponents
        }

        override fun getAllOutputs(): Collection<NEUIngredient> {
            return listOf(NEUIngredient.fromString(itemId.neuItem + ":1"))
        }
    }

    var recipes = listOf<EssenceUpgradeRecipe>()
        private set

    override fun reload(repository: NEURepository) {
        val recipes = mutableListOf<EssenceUpgradeRecipe>()
        for ((neuId, costs) in repository.constants.essenceCost.costs) {
            // TODO: add dungeonization costs. this is in repo, but not in the repo parser.
            for ((starCountAfter, essenceCost) in costs.essenceCosts.entries) {
                val items = costs.itemCosts[starCountAfter] ?: emptyList()
                recipes.add(
                    EssenceUpgradeRecipe(
                        SkyblockId(neuId),
                        starCountAfter,
                        essenceCost,
                        "ESSENCE_" + costs.type.uppercase(), // how flimsy
                        items.map { NEUIngredient.fromString(it) }))
            }
        }
        this.recipes = recipes
    }
}

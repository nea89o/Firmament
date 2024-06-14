/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import java.util.NavigableMap
import java.util.TreeMap
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.skyblockId

object ItemNameLookup : IReloadable {

    fun getItemNameChunks(name: String): Set<String> {
        return name.removeColorCodes().split(" ").filterTo(mutableSetOf()) { it.isNotBlank() }
    }

    var nameMap: NavigableMap<String, out Set<SkyblockId>> = TreeMap()

    override fun reload(repository: NEURepository) {
        val nameMap = TreeMap<String, MutableSet<SkyblockId>>()
        repository.items.items.values.forEach { item ->
            getAllNamesForItem(item).forEach { name ->
                val chunks = getItemNameChunks(name)
                chunks.forEach { chunk ->
                    val set = nameMap.getOrPut(chunk, ::mutableSetOf)
                    set.add(item.skyblockId)
                }
            }
        }
        this.nameMap = nameMap
    }

    fun getAllNamesForItem(item: NEUItem): Set<String> {
        val names = mutableSetOf<String>()
        names.add(item.displayName)
        if (item.displayName.contains("Enchanted Book")) {
            val enchantName = item.lore.firstOrNull()
            if (enchantName != null) {
                names.add(enchantName)
            }
        }
        return names
    }

    fun findItemCandidatesByName(name: String): MutableSet<SkyblockId> {
        val candidates = mutableSetOf<SkyblockId>()
        for (chunk in getItemNameChunks(name)) {
            val set = nameMap[chunk] ?: emptySet()
            candidates.addAll(set)
        }
        return candidates
    }


    fun guessItemByName(
        /**
         * The display name of the item. Color codes will be ignored.
         */
        name: String,
        /**
         * Whether the [name] may contain other text, such as reforges, master stars and such.
         */
        mayBeMangled: Boolean
    ): SkyblockId? {
        val cleanName = name.removeColorCodes()
        return findBestItemFromCandidates(
            findItemCandidatesByName(cleanName),
            cleanName,
            true
        )
    }

    fun findBestItemFromCandidates(
        candidates: Iterable<SkyblockId>,
        name: String, mayBeMangled: Boolean
    ): SkyblockId? {
        val expectedClean = name.removeColorCodes()
        var bestMatch: SkyblockId? = null
        var bestMatchLength = -1
        for (candidate in candidates) {
            val item = RepoManager.getNEUItem(candidate) ?: continue
            for (name in getAllNamesForItem(item)) {
                val actualClean = name.removeColorCodes()
                val matches = if (mayBeMangled) expectedClean == actualClean
                else expectedClean.contains(actualClean)
                if (!matches) continue
                if (actualClean.length > bestMatchLength) {
                    bestMatch = candidate
                    bestMatchLength = actualClean.length
                }
            }
        }
        return bestMatch
    }

}

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei

import io.github.moulberry.repo.constants.PetNumbers
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import java.util.stream.Stream
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.repo.ExpLadders
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.HypixelPetInfo
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.appendLore
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.skyBlockId

// TODO: add in extra data like pet info, into this structure
data class PetData(
    val rarity: Rarity,
    val petId: String,
    val exp: Double,
    val isStub: Boolean = false,
) {
    companion object {
        fun fromHypixel(petInfo: HypixelPetInfo) = PetData(
            petInfo.tier, petInfo.type, petInfo.exp,
        )
    }

    val levelData by lazy { ExpLadders.getExpLadder(petId, rarity).getPetLevel(exp) }
}

data class SBItemStack(
    val skyblockId: SkyblockId,
    val neuItem: NEUItem?,
    val stackSize: Int,
    val petData: PetData?,
    val extraLore: List<Text> = emptyList(),
) {
    constructor(skyblockId: SkyblockId, petData: PetData) : this(
        skyblockId,
        RepoManager.getNEUItem(skyblockId),
        1,
        petData
    )

    constructor(skyblockId: SkyblockId, stackSize: Int = 1) : this(
        skyblockId,
        RepoManager.getNEUItem(skyblockId),
        stackSize,
        RepoManager.getPotentialStubPetData(skyblockId)
    )

    private fun injectReplacementDataForPetLevel(
        petInfo: PetNumbers,
        level: Int,
        replacementData: MutableMap<String, String>
    ) {
        val stats = petInfo.interpolatedStatsAtLevel(level) ?: return
        stats.otherNumbers.forEachIndexed { index, it ->
            replacementData[index.toString()] = FirmFormatters.formatCurrency(it, 1)
        }
        stats.statNumbers.forEach { (t, u) ->
            replacementData[t] = FirmFormatters.formatCurrency(u, 1)
        }
    }

    private fun injectReplacementDataForPets(replacementData: MutableMap<String, String>) {
        if (petData == null) return
        val petInfo = RepoManager.neuRepo.constants.petNumbers[petData.petId]?.get(petData.rarity) ?: return
        if (petData.isStub) {
            val mapLow = mutableMapOf<String, String>()
            injectReplacementDataForPetLevel(petInfo, petInfo.lowLevel, mapLow)
            val mapHigh = mutableMapOf<String, String>()
            injectReplacementDataForPetLevel(petInfo, petInfo.highLevel, mapHigh)
            mapHigh.forEach { (key, highValue) ->
                mapLow.merge(key, highValue) { a, b -> "$a → $b" }
            }
            replacementData.putAll(mapLow)
            replacementData["LVL"] = "${petInfo.lowLevel} → ${petInfo.highLevel}"
        } else {
            injectReplacementDataForPetLevel(petInfo, petData.levelData.currentLevel, replacementData)
            replacementData["LVL"] = petData.levelData.currentLevel.toString()
        }
    }

    private val itemStack: ItemStack by lazy(LazyThreadSafetyMode.NONE) {
        if (skyblockId == SkyblockId.COINS)
            return@lazy ItemCache.coinItem(stackSize).also { it.appendLore(extraLore) }
        val replacementData = mutableMapOf<String, String>()
        injectReplacementDataForPets(replacementData)
        return@lazy neuItem.asItemStack(idHint = skyblockId, replacementData).copyWithCount(stackSize)
            .also { it.appendLore(extraLore) }
    }

    fun asImmutableItemStack(): ItemStack {
        return itemStack
    }

    fun asItemStack(): ItemStack {
        return itemStack.copy()
    }
}

object SBItemEntryDefinition : EntryDefinition<SBItemStack> {
    override fun equals(o1: SBItemStack, o2: SBItemStack, context: ComparisonContext): Boolean {
        return o1.skyblockId == o2.skyblockId && o1.stackSize == o2.stackSize
    }

    override fun cheatsAs(entry: EntryStack<SBItemStack>?, value: SBItemStack): ItemStack {
        return value.asItemStack()
    }

    override fun getValueType(): Class<SBItemStack> = SBItemStack::class.java
    override fun getType(): EntryType<SBItemStack> = EntryType.deferred(FirmamentReiPlugin.SKYBLOCK_ITEM_TYPE_ID)

    override fun getRenderer(): EntryRenderer<SBItemStack> = NEUItemEntryRenderer

    override fun getSerializer(): EntrySerializer<SBItemStack> {
        return NEUItemEntrySerializer
    }

    override fun getTagsFor(entry: EntryStack<SBItemStack>?, value: SBItemStack?): Stream<out TagKey<*>>? {
        return Stream.empty()
    }

    override fun asFormattedText(entry: EntryStack<SBItemStack>, value: SBItemStack): Text {
        return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemEntry(), value.asItemStack())
    }

    override fun hash(entry: EntryStack<SBItemStack>, value: SBItemStack, context: ComparisonContext): Long {
        // Repo items are immutable, and get replaced entirely when loaded from disk
        return value.skyblockId.hashCode() * 31L
    }

    override fun wildcard(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
        return value.copy(stackSize = 1)
    }

    override fun normalize(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
        return value.copy(stackSize = 1)
    }

    override fun copy(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
        return value
    }

    override fun isEmpty(entry: EntryStack<SBItemStack>?, value: SBItemStack): Boolean {
        return value.stackSize == 0
    }

    override fun getIdentifier(entry: EntryStack<SBItemStack>?, value: SBItemStack): Identifier {
        return value.skyblockId.identifier
    }

    fun getEntry(sbItemStack: SBItemStack): EntryStack<SBItemStack> =
        EntryStack.of(this, sbItemStack)

    fun getEntry(skyblockId: SkyblockId, count: Int = 1): EntryStack<SBItemStack> =
        getEntry(SBItemStack(skyblockId, count))

    fun getEntry(ingredient: NEUIngredient): EntryStack<SBItemStack> =
        getEntry(SkyblockId(ingredient.itemId), count = ingredient.amount.toInt())

    fun getEntry(stack: ItemStack): EntryStack<SBItemStack> =
        getEntry(
            SBItemStack(
                stack.skyBlockId ?: SkyblockId.NULL,
                RepoManager.getNEUItem(stack.skyBlockId ?: SkyblockId.NULL),
                stack.count,
                petData = stack.petData?.let { PetData.fromHypixel(it) }
            )
        )
}

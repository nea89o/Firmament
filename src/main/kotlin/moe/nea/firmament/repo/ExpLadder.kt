package moe.nea.firmament.repo

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.constants.PetLevelingBehaviourOverride
import io.github.moulberry.repo.data.Rarity

object ExpLadders : IReloadable {

    data class PetLevel(
        val currentLevel: Int,
        val maxLevel: Int,
        val expRequiredForNextLevel: Long,
        val expRequiredForMaxLevel: Long,
        val expInCurrentLevel: Float,
        var expTotal: Float,
    ) {
        val percentageToNextLevel: Float = expInCurrentLevel / expRequiredForNextLevel
    }

    data class ExpLadder(
        val individualLevelCost: List<Long>,
    ) {
        val cumulativeLevelCost = individualLevelCost.runningFold(0F) { a, b -> a + b }.map { it.toLong() }
        fun getPetLevel(currentExp: Double): PetLevel {
            val currentOneIndexedLevel = cumulativeLevelCost.indexOfLast { it <= currentExp } + 1
            val expForNextLevel = if (currentOneIndexedLevel > individualLevelCost.size) { // Max leveled pet
                individualLevelCost.last()
            } else {
                individualLevelCost[currentOneIndexedLevel - 1]
            }
            val expInCurrentLevel =
                if (currentOneIndexedLevel >= cumulativeLevelCost.size)
                    currentExp.toFloat() - cumulativeLevelCost.last()
                else
                    (expForNextLevel - (cumulativeLevelCost[currentOneIndexedLevel] - currentExp.toFloat())).coerceAtLeast(
                        0F
                    )
            return PetLevel(
                currentLevel = currentOneIndexedLevel,
                maxLevel = cumulativeLevelCost.size,
                expRequiredForNextLevel = expForNextLevel,
                expRequiredForMaxLevel = cumulativeLevelCost.last(),
                expInCurrentLevel = expInCurrentLevel,
                expTotal = currentExp.toFloat()
            )
        }

        fun getPetExpForLevel(level: Int): Long {
            if (level < 2) return 0L
            if (level >= cumulativeLevelCost.size) return cumulativeLevelCost.last()
            return cumulativeLevelCost[level - 1]
        }
    }

    private data class Key(val petIdWithoutRarity: String, val rarity: Rarity)

    private val expLadders = CacheBuilder.newBuilder()
        .build(object : CacheLoader<Key, ExpLadder>() {
            override fun load(key: Key): ExpLadder {
                val pld = RepoManager.neuRepo.constants.petLevelingData
                var exp = pld.petExpCostForLevel
                var offset = pld.petLevelStartOffset[key.rarity]!!
                var maxLevel = 100
                val override = pld.petLevelingBehaviourOverrides[key.petIdWithoutRarity]
                if (override != null) {
                    maxLevel = override.maxLevel ?: maxLevel
                    offset = override.petLevelStartOffset?.get(key.rarity) ?: offset
                    when (override.petExpCostModifierType) {
                        PetLevelingBehaviourOverride.PetExpModifierType.APPEND ->
                            exp = exp + override.petExpCostModifier

                        PetLevelingBehaviourOverride.PetExpModifierType.REPLACE ->
                            exp = override.petExpCostModifier

                        null -> {}
                    }
                }
                return ExpLadder(exp.drop(offset).take(maxLevel - 1).map { it.toLong() })
            }
        })

    override fun reload(repository: NEURepository?) {
        expLadders.invalidateAll()
    }

    fun getExpLadder(petId: String, rarity: Rarity): ExpLadder {
        return expLadders.get(Key(petId, rarity))
    }
}

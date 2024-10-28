package moe.nea.firmament.repo

import io.github.moulberry.repo.data.Rarity
import moe.nea.firmament.util.HypixelPetInfo

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

		fun forLevel(petId: String, rarity: Rarity, level: Int) = PetData(
			rarity, petId, ExpLadders.getExpLadder(petId, rarity).getPetExpForLevel(level).toDouble()
		)
	}

	val levelData by lazy { ExpLadders.getExpLadder(petId, rarity).getPetLevel(exp) }
}

package moe.nea.firmament.repo

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.moulberry.repo.constants.PetNumbers
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.withFallback
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.mc.appendLore
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.withColor

data class SBItemStack constructor(
	val skyblockId: SkyblockId,
	val neuItem: NEUItem?,
	private var stackSize: Int,
	private var petData: PetData?,
	val extraLore: List<Text> = emptyList(),
	// TODO: grab this star data from nbt if possible
	val stars: Int = 0,
	val fallback: ItemStack? = null,
) {

	fun getStackSize() = stackSize
	fun setStackSize(newSize: Int) {
		this.stackSize = newSize
		this.itemStack_ = null
	}

	fun getPetData() = petData
	fun setPetData(petData: PetData?) {
		this.petData = petData
		this.itemStack_ = null
	}

	companion object {
		val PACKET_CODEC: PacketCodec<in RegistryByteBuf, SBItemStack> = PacketCodec.tuple(
			SkyblockId.PACKET_CODEC, { it.skyblockId },
			PacketCodecs.VAR_INT, { it.stackSize },
			{ id, count -> SBItemStack(id, count) }
		)
		val CODEC: Codec<SBItemStack> = RecordCodecBuilder.create {
			it.group(
				SkyblockId.CODEC.fieldOf("skyblockId").forGetter { it.skyblockId },
				Codec.INT.fieldOf("count").forGetter { it.stackSize },
			).apply(it) { id, count ->
				SBItemStack(id, count)
			}
		}
		val EMPTY = SBItemStack(SkyblockId.NULL, 0)

		operator fun invoke(itemStack: ItemStack): SBItemStack {
			val skyblockId = itemStack.skyBlockId ?: SkyblockId.NULL
			return SBItemStack(
				skyblockId,
				RepoManager.getNEUItem(skyblockId),
				itemStack.count,
				petData = itemStack.petData?.let { PetData.fromHypixel(it) }
			)
		}

		operator fun invoke(neuIngredient: NEUIngredient): SBItemStack? {
			if (neuIngredient.skyblockId == SkyblockId.SENTINEL_EMPTY) return null // TODO: better fallback, maybe?
			if (neuIngredient.skyblockId == SkyblockId.COINS) {
				// TODO: specially handle coins to include the decimals
			}
			return SBItemStack(neuIngredient.skyblockId, neuIngredient.amount.toInt())
		}

		fun passthrough(itemStack: ItemStack): SBItemStack {
			return SBItemStack(SkyblockId.NULL, null, itemStack.count, null, fallback = itemStack)
		}
	}

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
			replacementData[index.toString()] = FirmFormatters.formatCommas(it, 1)
		}
		stats.statNumbers.forEach { (t, u) ->
			replacementData[t] = FirmFormatters.formatCommas(u, 1)
		}
	}

	private fun injectReplacementDataForPets(replacementData: MutableMap<String, String>) {
		val petData = this.petData ?: return
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


	private var itemStack_: ItemStack? = null

	private val itemStack: ItemStack
		get() {
			val itemStack = itemStack_ ?: run {
				if (skyblockId == SkyblockId.COINS)
					return@run ItemCache.coinItem(stackSize).also { it.appendLore(extraLore) }
				if (stackSize == 0)
					return@run ItemStack.EMPTY
				val replacementData = mutableMapOf<String, String>()
				injectReplacementDataForPets(replacementData)
				return@run neuItem.asItemStack(idHint = skyblockId, replacementData)
					.withFallback(fallback)
					.copyWithCount(stackSize)
					.also { it.appendLore(extraLore) }
					.also { enhanceStatsByStars(it, stars) }
			}
			if (itemStack_ == null)
				itemStack_ = itemStack
			return itemStack
		}


	private fun starString(stars: Int): Text {
		if (stars <= 0) return Text.empty()
		val tiers = listOf(
			LegacyFormattingCode.GOLD,
			LegacyFormattingCode.LIGHT_PURPLE,
			LegacyFormattingCode.AQUA,
		)
		val maxStars = 5
		if (stars > tiers.size * maxStars) return Text.literal(" ${stars}✪").withColor(Formatting.RED)
		val starBaseTier = (stars - 1) / maxStars
		val starBaseColor = tiers[starBaseTier]
		val starsInCurrentTier = stars - starBaseTier * maxStars
		val starString = Text.literal(" " + "✪".repeat(starsInCurrentTier)).withColor(starBaseColor.modern)
		if (starBaseTier > 0) {
			val starLastTier = tiers[starBaseTier - 1]
			val starsInLastTier = 5 - starsInCurrentTier
			starString.append(Text.literal("✪".repeat(starsInLastTier)).withColor(starLastTier.modern))
		}
		return starString
	}

	private fun enhanceStatsByStars(itemStack: ItemStack, stars: Int) {
		if (stars == 0) return
		// TODO: increase stats and add the star level into the nbt data so star displays work
		itemStack.displayNameAccordingToNbt = itemStack.displayNameAccordingToNbt.copy()
			.append(starString(stars))
	}

	fun asImmutableItemStack(): ItemStack {
		return itemStack
	}

	fun asCopiedItemStack(): ItemStack {
		return itemStack.copy()
	}
}

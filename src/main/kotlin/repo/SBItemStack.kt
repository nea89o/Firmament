package moe.nea.firmament.repo

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.moulberry.repo.constants.PetNumbers
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import util.skyblock.stats.StatFormatting
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.withFallback
import moe.nea.firmament.util.skyblock.stats.StatBlock
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.blue
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.getReforgeId
import moe.nea.firmament.util.getUpgradeStars
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.appendLore
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.mc.modifyLore
import moe.nea.firmament.util.modifyExtraAttributes
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.prepend
import moe.nea.firmament.util.reconstitute
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblock.stats.BuffKind
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

data class SBItemStack constructor(
	val skyblockId: SkyblockId,
	val neuItem: NEUItem?,
	private var stackSize: Int,
	private var petData: PetData?,
	val extraLore: List<Text> = emptyList(),
	val stars: Int = 0,
	val fallback: ItemStack? = null,
	val reforge: ReforgeId? = null,
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

		private val BREAKING_POWER_REGEX = "Breaking Power (?<power>[0-9]+)".toPattern()
		operator fun invoke(itemStack: ItemStack): SBItemStack {
			val skyblockId = itemStack.skyBlockId ?: SkyblockId.NULL
			return SBItemStack(
				skyblockId,
				RepoManager.getNEUItem(skyblockId),
				itemStack.count,
				petData = itemStack.petData?.let { PetData.fromHypixel(it) },
				stars = itemStack.getUpgradeStars(),
				reforge = itemStack.getReforgeId()
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

		fun appendEnhancedStats(
			itemStack: ItemStack,
			reforgeStats: Map<String, Double>,
			buffKind: BuffKind,
		) {
			val namedReforgeStats = reforgeStats
				.mapKeysTo(mutableMapOf()) { StatFormatting.statIdToName(it.key) }

			val loreMut = itemStack.loreAccordingToNbt.toMutableList()
			val statBlock = StatBlock.fromLore(loreMut)
			for ((statName, statBuff) in namedReforgeStats) {
				statBlock.modify(statName) { it.addStat(statBuff, buffKind) }
			}
			statBlock.applyModifications(loreMut)
			itemStack.loreAccordingToNbt = loreMut
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


	private fun appendReforgeInfo(
		itemStack: ItemStack,
	) {
		val rarity = Rarity.fromItem(itemStack) ?: return
		val reforgeId = this.reforge ?: return
		val reforge = ReforgeStore.modifierLut[reforgeId] ?: return
		val reforgeStats = reforge.reforgeStats?.get(rarity) ?: mapOf()
		itemStack.displayNameAccordingToNbt = itemStack.displayNameAccordingToNbt.copy()
			.prepend(Text.literal(reforge.reforgeName + " ").formatted(Rarity.colourMap[rarity] ?: Formatting.WHITE))
		val data = itemStack.extraAttributes.copy()
		data.putString("modifier", reforgeId.id)
		itemStack.extraAttributes = data
		appendEnhancedStats(itemStack, reforgeStats, BuffKind.REFORGE)
		reforge.reforgeAbility?.get(rarity)?.let { reforgeAbility ->
			val formattedReforgeAbility = ItemCache.un189Lore(reforgeAbility)
				.grey()
			itemStack.modifyLore {
				val lastBlank = it.indexOfLast { it.unformattedString.isBlank() }
				val newList = mutableListOf<Text>()
				newList.addAll(it.subList(0, lastBlank))
				newList.add(Text.literal(""))
				newList.add(Text.literal("${reforge.reforgeName} Bonus").blue())
				MC.font.textHandler.wrapLines(formattedReforgeAbility, 180, Style.EMPTY).mapTo(newList) {
					it.reconstitute()
				}
				newList.addAll(it.subList(lastBlank, it.size))
				return@modifyLore newList
			}
		}
	}

	// TODO: avoid instantiating the item stack here
	val itemType: ItemType? get() = ItemType.fromItemStack(asImmutableItemStack())
	val rarity: Rarity? get() = Rarity.fromItem(asImmutableItemStack())

	private var itemStack_: ItemStack? = null

	val breakingPower: Int
		get() =
			BREAKING_POWER_REGEX.useMatch(neuItem?.lore?.firstOrNull()?.removeColorCodes()) {
				group("power").toInt()
			} ?: 0

	private val itemStack: ItemStack
		get() {
			val itemStack = itemStack_ ?: run {
				if (skyblockId == SkyblockId.COINS)
					return@run ItemCache.coinItem(stackSize).also { it.appendLore(extraLore) }
				if (stackSize == 0)
					return@run ItemStack.EMPTY
				val replacementData = mutableMapOf<String, String>()
				injectReplacementDataForPets(replacementData)
				val baseItem = neuItem.asItemStack(idHint = skyblockId, replacementData)
					.withFallback(fallback)
					.copyWithCount(stackSize)
				val baseStats = StatBlock.fromLore(baseItem.loreAccordingToNbt)
				appendReforgeInfo(baseItem)
				baseItem.appendLore(extraLore)
				enhanceStatsByStars(baseItem, stars, baseStats)
				return@run baseItem
			}
			if (itemStack_ == null)
				itemStack_ = itemStack
			return itemStack
		}


	private fun starString(stars: Int): Text {
		if (stars <= 0) return Text.empty()
		// TODO: idk master stars
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

	private fun enhanceStatsByStars(itemStack: ItemStack, stars: Int, baseStats: StatBlock) {
		if (stars == 0) return
		itemStack.modifyExtraAttributes {
			it.putInt("upgrade_level", stars)
		}
		itemStack.displayNameAccordingToNbt = itemStack.displayNameAccordingToNbt.copy()
			.append(starString(stars))
		val isDungeon = ItemType.fromItemStack(itemStack)?.isDungeon ?: true
		val truncatedStarCount = if (isDungeon) minOf(5, stars) else stars
		appendEnhancedStats(itemStack,
		                    baseStats
			                    .indexedByName.values
			                    .filter { it.stat.isStarAffected }
			                    .associate {
				                    it.statName to (it.value * (truncatedStarCount * 0.02))
			                    },
		                    BuffKind.STAR_BUFF)
	}

	fun asImmutableItemStack(): ItemStack {
		return itemStack
	}

	fun asCopiedItemStack(): ItemStack {
		return itemStack.copy()
	}
}

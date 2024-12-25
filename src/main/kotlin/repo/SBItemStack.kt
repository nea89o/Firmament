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
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.getReforgeId
import moe.nea.firmament.util.getUpgradeStars
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.appendLore
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.prepend
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

data class SBItemStack constructor(
	val skyblockId: SkyblockId,
	val neuItem: NEUItem?,
	private var stackSize: Int,
	private var petData: PetData?,
	val extraLore: List<Text> = emptyList(),
	val stars: Int = 0,
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

		fun appendEnhancedStats(
			itemStack: ItemStack,
			reforgeStats: Map<String, Double>,
			buffKind: BuffKind,
		) {
			val namedReforgeStats = reforgeStats
				.mapKeysTo(mutableMapOf()) { statIdToName(it.key) }
			val loreMut = itemStack.loreAccordingToNbt.toMutableList()
			var statBlockLastIndex = -1
			for (i in loreMut.indices) {
				val statLine = parseStatLine(loreMut[i])
				if (statLine == null && statBlockLastIndex >= 0) {
					break
				}
				if (statLine == null) {
					continue
				}
				statBlockLastIndex = i
				val statBuff = namedReforgeStats.remove(statLine.statName) ?: continue
				loreMut[i] = statLine.addStat(statBuff, buffKind).reconstitute()
			}
			if (namedReforgeStats.isNotEmpty() && statBlockLastIndex == -1) {
				loreMut.add(0, Text.literal(""))
			}
			// If there is no stat block the statBlockLastIndex falls through to -1
			// TODO: this is good enough for some items. some other items might have their stats at a different place.
			for ((statName, statBuff) in namedReforgeStats) {
				val statLine = StatLine(statName, null).addStat(statBuff, buffKind)
				loreMut.add(statBlockLastIndex + 1, statLine.reconstitute())
			}
			itemStack.loreAccordingToNbt = loreMut
		}

		data class StatFormatting(
			val postFix: String,
			val color: Formatting,
		)

		val formattingOverrides = mapOf(
			"Sea Creature Chance" to StatFormatting("%", Formatting.RED),
			"Strength" to StatFormatting("", Formatting.RED),
			"Damage" to StatFormatting("", Formatting.RED),
			"Bonus Attack Speed" to StatFormatting("%", Formatting.RED),
			"Shot Cooldown" to StatFormatting("s", Formatting.RED),
			"Ability Damage" to StatFormatting("%", Formatting.RED),
			"Crit Damage" to StatFormatting("%", Formatting.RED),
			"Crit Chance" to StatFormatting("%", Formatting.RED),
			"Trophy Fish Chance" to StatFormatting("%", Formatting.GREEN),
			// TODO: add other types and make this a repo json
		)


		private val statLabelRegex = "(?<statName>.*): ".toPattern()

		enum class BuffKind(
			val color: Formatting,
			val prefix: String,
			val postFix: String,
		) {
			REFORGE(Formatting.BLUE, "(", ")"),

			;
		}

		data class StatLine(
			val statName: String,
			val value: Text?,
			val rest: List<Text> = listOf(),
			val valueNum: Double? = value?.directLiteralStringContent?.trim(' ', '%', '+')?.toDoubleOrNull()
		) {
			fun addStat(amount: Double, buffKind: BuffKind): StatLine {
				val formattedAmount = FirmFormatters.formatCommas(amount, 1, includeSign = true)
				return copy(
					valueNum = (valueNum ?: 0.0) + amount,
					value = null,
					rest = rest +
						listOf(
							Text.literal(
								buffKind.prefix + formattedAmount +
									statFormatting.postFix +
									buffKind.postFix + " ")
								.withColor(buffKind.color)))
			}

			fun formatValue() =
				Text.literal(FirmFormatters.formatCommas(valueNum ?: 0.0,
				                                         1,
				                                         includeSign = true) + statFormatting.postFix + " ")
					.setStyle(Style.EMPTY.withColor(statFormatting.color))

			val statFormatting = formattingOverrides[statName] ?: StatFormatting("", Formatting.GREEN)
			private fun abbreviate(abbreviateTo: Int): String {
				if (abbreviateTo >= statName.length) return statName
				val segments = statName.split(" ")
				return segments.joinToString(" ") {
					it.substring(0, maxOf(1, abbreviateTo / segments.size))
				}
			}

			fun reconstitute(abbreviateTo: Int = Int.MAX_VALUE): Text =
				Text.literal("").setStyle(Style.EMPTY.withItalic(false))
					.append(Text.literal("${abbreviate(abbreviateTo)}: ").grey())
					.append(value ?: formatValue())
					.also { rest.forEach(it::append) }
		}

		fun statIdToName(statId: String): String {
			val segments = statId.split("_")
			return segments.joinToString(" ") { it.replaceFirstChar { it.uppercaseChar() } }
		}

		private fun parseStatLine(line: Text): StatLine? {
			val sibs = line.siblings
			val stat = sibs.firstOrNull() ?: return null
			if (stat.style.color != TextColor.fromFormatting(Formatting.GRAY)) return null
			val statLabel = stat.directLiteralStringContent ?: return null
			val statName = statLabelRegex.useMatch(statLabel) { group("statName") } ?: return null
			return StatLine(statName, sibs[1], sibs.subList(2, sibs.size))
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
	}

	// TODO: avoid instantiating the item stack here
	val itemType: ItemType? get() = ItemType.fromItemStack(asImmutableItemStack())
	val rarity: Rarity? get() = Rarity.fromItem(asImmutableItemStack())

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
					.copyWithCount(stackSize)
					.also { appendReforgeInfo(it) }
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

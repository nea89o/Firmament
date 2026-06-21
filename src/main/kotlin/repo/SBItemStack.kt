package moe.nea.firmament.repo

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.moulberry.repo.constants.PetNumbers
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import net.minecraft.world.item.ItemStack
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.ChatFormatting
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.isBroken
import moe.nea.firmament.repo.ItemCache.withFallback
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.blue
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.getReforgeId
import moe.nea.firmament.util.getUpgradeStars
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.DataComponentAccessor
import moe.nea.firmament.util.mc.DataComponentMutator
import moe.nea.firmament.util.mc.LazyItemStack
import moe.nea.firmament.util.mc.accessor
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
import moe.nea.firmament.util.skyblockId
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

data class SBItemStack constructor(
	val skyblockId: SkyblockId,
	val neuItem: NEUItem?,
	private var stackSize: Int,
	private var petData: PetData?,
	val extraLore: List<Component> = emptyList(),
	val stars: Int = 0,
	val fallback: LazyItemStack? = null,
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
		val PACKET_CODEC: StreamCodec<in RegistryFriendlyByteBuf, SBItemStack> = StreamCodec.composite(
			SkyblockId.PACKET_CODEC, { it.skyblockId },
			ByteBufCodecs.VAR_INT, { it.stackSize },
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
		operator fun invoke(itemStack: ItemStack): SBItemStack=
			invoke(itemStack.accessor())
		operator fun invoke(itemStack: DataComponentAccessor): SBItemStack {
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

		fun passthrough(itemStack: LazyItemStack): SBItemStack {
			return SBItemStack(SkyblockId.NULL, null, itemStack.count, null, fallback = itemStack)
		}

		fun parseStatBlock(itemStack: DataComponentAccessor): List<StatLine> {
			return itemStack.loreAccordingToNbt
				.map { parseStatLine(it) }
				.takeWhile { it != null }
				.filterNotNull()
		}

		fun appendEnhancedStats(
			itemStack: DataComponentMutator,
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
				loreMut.add(0, Component.literal(""))
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
			val color: ChatFormatting,
			val isStarAffected: Boolean = true,
		)

		val formattingOverrides = mapOf(
			"Sea Creature Chance" to StatFormatting("%", ChatFormatting.RED),
			"Strength" to StatFormatting("", ChatFormatting.RED),
			"Damage" to StatFormatting("", ChatFormatting.RED),
			"Bonus Attack Speed" to StatFormatting("%", ChatFormatting.RED),
			"Shot Cooldown" to StatFormatting("s", ChatFormatting.GREEN, false),
			"Ability Damage" to StatFormatting("%", ChatFormatting.RED),
			"Crit Damage" to StatFormatting("%", ChatFormatting.RED),
			"Crit Chance" to StatFormatting("%", ChatFormatting.RED),
			"Ability Damage" to StatFormatting("%", ChatFormatting.RED),
			"Trophy Fish Chance" to StatFormatting("%", ChatFormatting.GREEN),
			"Health" to StatFormatting("", ChatFormatting.GREEN),
			"Defense" to StatFormatting("", ChatFormatting.GREEN),
			"Fishing Speed" to StatFormatting("", ChatFormatting.GREEN),
			"Double Hook Chance" to StatFormatting("%", ChatFormatting.GREEN),
			"Mining Speed" to StatFormatting("", ChatFormatting.GREEN),
			"Mining Fortune" to StatFormatting("", ChatFormatting.GREEN),
			"Heat Resistance" to StatFormatting("", ChatFormatting.GREEN),
			"Swing Range" to StatFormatting("", ChatFormatting.GREEN),
			"Rift Time" to StatFormatting("", ChatFormatting.GREEN),
			"Speed" to StatFormatting("", ChatFormatting.GREEN),
			"Farming Fortune" to StatFormatting("", ChatFormatting.GREEN),
			"True Defense" to StatFormatting("", ChatFormatting.GREEN),
			"Mending" to StatFormatting("", ChatFormatting.GREEN),
			"Foraging Wisdom" to StatFormatting("", ChatFormatting.GREEN),
			"Farming Wisdom" to StatFormatting("", ChatFormatting.GREEN),
			"Foraging Fortune" to StatFormatting("", ChatFormatting.GREEN),
			"Magic Find" to StatFormatting("", ChatFormatting.GREEN),
			"Ferocity" to StatFormatting("", ChatFormatting.GREEN),
			"Bonus Pest Chance" to StatFormatting("%", ChatFormatting.GREEN),
			"Cold Resistance" to StatFormatting("", ChatFormatting.GREEN),
			"Pet Luck" to StatFormatting("", ChatFormatting.GREEN),
			"Fear" to StatFormatting("", ChatFormatting.GREEN),
			"Mana Regen" to StatFormatting("%", ChatFormatting.GREEN),
			"Rift Damage" to StatFormatting("", ChatFormatting.GREEN),
			"Hearts" to StatFormatting("", ChatFormatting.GREEN),
			"Vitality" to StatFormatting("", ChatFormatting.GREEN),
			// TODO: make this a repo json
		)


		private val statLabelRegex = "(?<statName>.*): ".toPattern()

		enum class BuffKind(
			val color: ChatFormatting,
			val prefix: String,
			val postFix: String,
			val isHidden: Boolean,
		) {
			REFORGE(ChatFormatting.BLUE, "(", ")", false),
			STAR_BUFF(ChatFormatting.RESET, "", "", true),
			CATA_STAR_BUFF(ChatFormatting.DARK_GRAY, "(", ")", false),
			;
		}

		data class StatLine(
			val statName: String,
			val value: Component?,
			val rest: List<Component> = listOf(),
			val valueNum: Double? = value?.directLiteralStringContent?.trim(' ', 's', '%', '+')?.toDoubleOrNull()
		) {
			fun addStat(amount: Double, buffKind: BuffKind): StatLine {
				val formattedAmount = FirmFormatters.formatCommas(amount, 1, includeSign = true)
				return copy(
					valueNum = (valueNum ?: 0.0) + amount,
					value = null,
					rest = rest +
						if (buffKind.isHidden) emptyList()
						else listOf(
							Component.literal(
								buffKind.prefix + formattedAmount +
									statFormatting.postFix +
									buffKind.postFix + " "
							)
								.withColor(buffKind.color)
						)
				)
			}

			fun formatValue() =
				Component.literal(
					FirmFormatters.formatCommas(
						valueNum ?: 0.0,
						1,
						includeSign = true
					) + statFormatting.postFix + " "
				)
					.setStyle(Style.EMPTY.withColor(statFormatting.color))

			val statFormatting = formattingOverrides[statName] ?: StatFormatting("", ChatFormatting.GREEN)
			private fun abbreviate(abbreviateTo: Int): String {
				if (abbreviateTo >= statName.length) return statName
				val segments = statName.split(" ")
				return segments.joinToString(" ") {
					it.substring(0, maxOf(1, abbreviateTo / segments.size))
				}
			}

			fun reconstitute(abbreviateTo: Int = Int.MAX_VALUE): Component =
				Component.literal("").setStyle(Style.EMPTY.withItalic(false))
					.append(Component.literal("${abbreviate(abbreviateTo)}: ").grey())
					.append(value ?: formatValue())
					.also { rest.forEach(it::append) }
		}

		fun statIdToName(statId: String): String {
			val segments = statId.split("_")
			return segments.joinToString(" ") { it.replaceFirstChar { it.uppercaseChar() } }
		}

		fun parseStatLine(line: Component): StatLine? {
			val sibs = line.siblings
			val stat = sibs.firstOrNull() ?: return null
			if (stat.style.color != TextColor.fromLegacyFormat(ChatFormatting.GRAY)) return null
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
		itemStack: DataComponentMutator,
	) {
		val rarity = Rarity.fromItem(itemStack) ?: return
		val reforgeId = this.reforge ?: return
		val reforge = ReforgeStore.modifierLut[reforgeId] ?: return
		val reforgeStats = reforge.reforgeStats?.get(rarity) ?: mapOf()
		itemStack.displayNameAccordingToNbt = itemStack.displayNameAccordingToNbt.copy()
			.prepend(
				Component.literal(reforge.reforgeName + " ").withStyle(Rarity.colourMap[rarity] ?: ChatFormatting.WHITE)
			)
		val data = itemStack.extraAttributes.copy()
		data.putString("modifier", reforgeId.id)
		itemStack.extraAttributes = data
		appendEnhancedStats(itemStack, reforgeStats, BuffKind.REFORGE)
		reforge.reforgeAbility?.get(rarity)?.let { reforgeAbility ->
			val formattedReforgeAbility = ItemCache.un189Lore(reforgeAbility)
				.grey()
			itemStack.modifyLore {
				val lastBlank = it.indexOfLast { it.unformattedString.isBlank() }
				val newList = mutableListOf<Component>()
				newList.addAll(it.subList(0, lastBlank))
				newList.add(Component.literal(""))
				newList.add(Component.literal("${reforge.reforgeName} Bonus").blue())
				MC.font.splitter.splitLines(formattedReforgeAbility, 180, Style.EMPTY).mapTo(newList) {
					it.reconstitute()
				}
				newList.addAll(it.subList(lastBlank, it.size))
				return@modifyLore newList
			}
		}
	}

	// TODO: avoid instantiating the item stack here
	@ExpensiveItemCacheApi
	val itemType: ItemType? get() = ItemType.fromItemStack(asImmutableItemStack())

	@ExpensiveItemCacheApi
	val rarity: Rarity? get() = Rarity.fromItem(asImmutableItemStack())

	private var itemStack_: LazyItemStack? = null

	val breakingPower: Int
		get() =
			BREAKING_POWER_REGEX.useMatch(neuItem?.lore?.firstOrNull()?.removeColorCodes()) {
				group("power").toInt()
			} ?: 0

	@ExpensiveItemCacheApi
	private val itemStack: LazyItemStack
		get() {
			val itemStack = itemStack_ ?: run {
				if (skyblockId == SkyblockId.COINS)
					return@run ItemCache.coinItem(stackSize).withMutations { appendLore(extraLore) }
				if (stackSize == 0)
					return@run LazyItemStack.empty()
				val replacementData = mutableMapOf<String, String>()
				injectReplacementDataForPets(replacementData)
				val baseItem = neuItem.asItemStack(idHint = skyblockId, replacementData)
					.withFallback(fallback)
					.withCount(stackSize)
					.intoMutable()
				val baseStats = parseStatBlock(baseItem)
				appendReforgeInfo(baseItem)
				baseItem.appendLore(extraLore)
				enhanceStatsByStars(baseItem, stars, baseStats)
				return@run baseItem.finish()
			}
			if (itemStack_ == null && !itemStack.isBroken)
				itemStack_ = itemStack
			return itemStack
		}


	/**
	 * estimate the lore content without creating an itemstack instance
	 */
	fun estimateLore(): List<Component> {
		return (neuItem?.lore?.map { ItemCache.un189Lore(it) } ?: emptyList()) + extraLore
	}

	private fun starString(stars: Int): Component {
		if (stars <= 0) return Component.empty()
		// TODO: idk master stars
		val tiers = listOf(
			LegacyFormattingCode.GOLD,
			LegacyFormattingCode.LIGHT_PURPLE,
			LegacyFormattingCode.AQUA,
		)
		val maxStars = 5
		if (stars > tiers.size * maxStars) return Component.literal(" ${stars}✪").withColor(ChatFormatting.RED)
		val starBaseTier = (stars - 1) / maxStars
		val starBaseColor = tiers[starBaseTier]
		val starsInCurrentTier = stars - starBaseTier * maxStars
		val starString = Component.literal(" " + "✪".repeat(starsInCurrentTier)).withColor(starBaseColor.modern)
		if (starBaseTier > 0) {
			val starLastTier = tiers[starBaseTier - 1]
			val starsInLastTier = 5 - starsInCurrentTier
			starString.append(Component.literal("✪".repeat(starsInLastTier)).withColor(starLastTier.modern))
		}
		return starString
	}

	private fun enhanceStatsByStars(itemStack: DataComponentMutator, stars: Int, baseStats: List<StatLine>) {
		if (stars == 0) return
		// TODO: increase stats and add the star level into the nbt data so star displays work
		itemStack.modifyExtraAttributes {
			it.putInt("upgrade_level", stars)
		}
		itemStack.displayNameAccordingToNbt = itemStack.displayNameAccordingToNbt.copy()
			.append(starString(stars))
		val isDungeon = ItemType.fromItemStack(itemStack)?.isDungeon ?: true
		val truncatedStarCount = if (isDungeon) minOf(5, stars) else stars
		appendEnhancedStats(
			itemStack,
			baseStats
				.filter { it.statFormatting.isStarAffected }
				.associate {
					it.statName to ((it.valueNum ?: 0.0) * (truncatedStarCount * 0.02))
				},
			BuffKind.STAR_BUFF
		)
	}

	fun isWarm(): Boolean {
		if (itemStack_ != null) return true
		if (ItemCache.hasCacheFor(skyblockId)) return true
		return false
	}

	@OptIn(ExpensiveItemCacheApi::class)
	fun asLazyImmutableItemStack(): LazyItemStack? {
		if (isWarm()) return itemStack
		return null
	}

	@ExpensiveItemCacheApi
	fun asImmutableItemStack(): LazyItemStack { // TODO: add a "or fallback to painting" option to asLazyImmutableItemStack to be used in more places.
		return itemStack
	}
}

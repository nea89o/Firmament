package moe.nea.firmament.util.skyblock

import kotlin.time.Duration
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.parseTimePattern
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object AbilityUtils {
	data class ItemAbility(
		val name: String,
		val hasPowerScroll: Boolean,
		val activation: AbilityActivation,
		val manaCost: Int?,
		val descriptionLines: List<Text>,
		val cooldown: Duration?,
	)

	@JvmInline
	value class AbilityActivation(
		val label: String
	) {
		companion object {
			val RIGHT_CLICK = AbilityActivation("RIGHT CLICK")
			val SNEAK_RIGHT_CLICK = AbilityActivation("SNEAK RIGHT CLICK")
			val SNEAK = AbilityActivation("SNEAK")
			val EMPTY = AbilityActivation("")
			fun of(text: String?): AbilityActivation {
				val trimmed = text?.trim()
				if (trimmed.isNullOrBlank())
					return EMPTY
				return AbilityActivation(trimmed)
			}
		}
	}

	private val abilityNameRegex = "Ability: (?<name>.*?) *".toPattern()
	private fun findAbility(iterator: ListIterator<Text>): ItemAbility? {
		if (!iterator.hasNext()) {
			return null
		}
		val line = iterator.next()
		// The actual information about abilities is stored in the siblings
		if (line.directLiteralStringContent != "") return null
		var powerScroll: Boolean = false // This should instead determine the power scroll based on text colour
		var abilityName: String? = null
		var activation: String? = null
		var hasProcessedActivation = false
		for (sibling in line.siblings) {
			val directContent = sibling.directLiteralStringContent ?: continue
			if (directContent == "⦾ ") {
				powerScroll = true
				continue
			}
			if (!hasProcessedActivation && abilityName != null) {
				hasProcessedActivation = true
				activation = directContent
				continue
			}
			abilityNameRegex.useMatch<Nothing>(directContent) {
				abilityName = group("name")
				continue
			}
			if (abilityName != null) {
				ErrorUtil.softError("Found abilityName $abilityName without finding but encountered unprocessable element in: $line")
			}
			return null
		}
		if (abilityName == null) return null
		val descriptionLines = mutableListOf<Text>()
		var manaCost: Int? = null
		var cooldown: Duration? = null
		while (iterator.hasNext()) {
			val descriptionLine = iterator.next()
			if (descriptionLine.unformattedString == "") break
			var nextIsManaCost = false
			var isSpecialLine = false
			var nextIsDuration = false
			for (sibling in descriptionLine.siblings) {
				val directContent = sibling.directLiteralStringContent ?: continue
				if ("Mana Cost: " == directContent) { // TODO: 'Soulflow Cost: ' support (or maybe a generic 'XXX Cost: ')
					nextIsManaCost = true
					isSpecialLine = true
					continue
				}
				if ("Cooldown: " == directContent) {
					nextIsDuration = true
					isSpecialLine = true
					continue
				}
				if (nextIsDuration) {
					nextIsDuration = false
					cooldown = parseTimePattern(directContent)
					continue
				}
				if (nextIsManaCost) {
					nextIsManaCost = false
					manaCost = parseShortNumber(directContent).toInt()
					continue
				}
				if (isSpecialLine) {
					ErrorUtil.softError("Unknown special line segment: '$sibling' in '$descriptionLine'")
				}
			}
			if (!isSpecialLine) {
				descriptionLines.add(descriptionLine)
			}
		}
		return ItemAbility(
			abilityName,
			powerScroll,
			AbilityActivation.of(activation),
			manaCost,
			descriptionLines,
			cooldown
		)
	}

	fun getAbilities(lore: List<Text>): List<ItemAbility> {
		val iterator = lore.listIterator()
		val abilities = mutableListOf<ItemAbility>()
		while (iterator.hasNext()) {
			findAbility(iterator)?.let(abilities::add)
		}

		return abilities
	}

	// TODO: memoize
	fun getAbilities(itemStack: ItemStack): List<ItemAbility> {
		return getAbilities(itemStack.loreAccordingToNbt)
	}

}

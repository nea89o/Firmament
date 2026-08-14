package moe.nea.firmament.util.skyblock

import moe.nea.firmament.util.removeColorCodes

/**
 * Utilities for reading the enchantment name out of the lore of an enchanted book.
 *
 * Newer books prefix their lore with a [COMBINABLE_IN_ANVIL] header and a blank line, older ones start with the
 * enchantment name directly.
 */
object EnchantedBookLore {
	const val COMBINABLE_IN_ANVIL = "Combinable in Anvil"

	/**
	 * Returns the (still formatted) lore line holding the enchantment name, or `null` if there is none.
	 */
	fun findEnchantmentName(lore: Sequence<String>): String? {
		val iterator = lore.iterator()
		if (!iterator.hasNext()) return null
		val firstLine = iterator.next()
		if (firstLine.removeColorCodes().trim() != COMBINABLE_IN_ANVIL) return firstLine
		while (iterator.hasNext()) {
			val line = iterator.next()
			if (line.removeColorCodes().isNotBlank()) return line
		}
		return null
	}

	fun findEnchantmentName(lore: Iterable<String>): String? = findEnchantmentName(lore.asSequence())
}

// Current Issues: The repo is outdated and does not support maxLevel yet
// Check status at https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/issues/2311
package moe.nea.firmament.features.inventory

import java.util.Optional
import org.lwjgl.glfw.GLFW
import kotlin.jvm.optionals.getOrNull
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.darkGrey
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.IntUtil.toRomanNumeral
import moe.nea.firmament.util.grey

object MissingEnchantments {
	fun itemTypeToEnchantKey(itemType: ItemType): String {
		return itemType.nonDungeonVariant.toString().replace(" ", "")
	}

	@Config
	object TConfig : ManagedConfig("missing-enchantments", Category.INVENTORY) {
		val enabled by toggle("enabled") { true }
		val enableKeybinding by keyBinding("show-missing-enchantments") { GLFW.GLFW_KEY_LEFT_SHIFT }
		val showUpgradableEnchantments by toggle("show-upgradable-enchantments") { true }
		val showConflictingEnchantments by toggle("show-conflicting-enchantments") { false }
	}

	fun enchantIdToText(enchId: String, level: Int? = null): String? {
		val skyblockId = SkyblockId("${enchId.uppercase()};${level ?: 1}")
		val displayName = RepoManager.getNEUItem(skyblockId)
			?.lore?.firstOrNull()

		return if (level == null) {
			displayName?.replace(Regex(" [IVX]+$"), "")
		} else {
			displayName
		}
	}


	@Subscribe
	fun onItemTooltip(it: ItemTooltipEvent) {
		if (!TConfig.enabled) return
		if (TConfig.enableKeybinding.isBound && !TConfig.enableKeybinding.isPressed()) return

		val itemType = ItemType.fromItemStack(it.stack)
		val pools = RepoManager.enchantData.allEnchants?.enchantPools
		val maxLevels = RepoManager.enchantData.enchantMaxLevels

		val appliedEnchantmentsData = it.stack.extraAttributes.getCompound("enchantments").getOrNull()
		val appliedEnchantments = appliedEnchantmentsData?.keySet().orEmpty()
		val appliedEnchantmentLevels = appliedEnchantmentsData?.keySet()?.associateWith { enchId -> appliedEnchantmentsData.getInt(enchId) }
		val enchantKey = itemType?.let { itemTypeToEnchantKey(it) } ?: return
		val neuEnchantmentData = RepoManager.enchantData.allEnchants?.availableEnchants[enchantKey] ?: return
		var missingEnchantments = neuEnchantmentData.minus(appliedEnchantmentsData?.keySet().orEmpty())

		// Shows Upgradable Enchantments Inline. For Example: "Sharpness V" will show as "Sharpness V → VII"
		if (TConfig.showUpgradableEnchantments) {
			for (i in it.lines.indices) {
				val line = it.lines[i]
				val lineText = line.unformattedString

				// Build map of enchantment text -> max level for upgradable enchants on this tooltip line
				val upgradeMap = mutableMapOf<String, Int>()
				for ((enchId, currentLevel) in appliedEnchantmentLevels.orEmpty()) {
					val enchantText = enchantIdToText(enchId, currentLevel.getOrNull())?.removeColorCodes()
					if (enchantText != null && enchantText in lineText) {
						val maxLevel = maxLevels[enchId]
						if (currentLevel.isPresent && maxLevel != null && currentLevel.get() < maxLevel) {
							upgradeMap[enchantText] = maxLevel
						}
					}
				}

				// If any enchantments can be upgraded, rebuild the line
				if (upgradeMap.isNotEmpty()) {
					val newLine = Component.literal("")
					line.visit({ style, string ->
						var remaining = string
						while (remaining.isNotEmpty()) {
							val match = upgradeMap.keys
								.mapNotNull { enchText -> remaining.indexOf(enchText).takeIf { it >= 0 }?.let { it to enchText } }
								.minByOrNull { it.first }
							if (match == null) {
								newLine.append(Component.literal(remaining).setStyle(style))
								break
							}
							val (idx, enchText) = match
							if (idx > 0) newLine.append(Component.literal(remaining.substring(0, idx)).setStyle(style))
							newLine.append(Component.literal(enchText).setStyle(style))
							newLine.append(Component.literal(" → ${upgradeMap[enchText]!!.toRomanNumeral()}").darkGrey())
							remaining = remaining.substring(idx + enchText.length)
						}
						Optional.empty()
					}, Style.EMPTY)
					it.lines[i] = newLine
				}
			}
		}

		// Removes Conflicting Enchantments from missingEnchantments
		if (!TConfig.showConflictingEnchantments) {
			for (missingEnchantment in missingEnchantments){
				for (pool in pools.orEmpty()){
					for (appliedEnchantment in appliedEnchantments) {
						if (missingEnchantment in pool && appliedEnchantment in pool) {
							for (conflictingEnchantment in pool) {
								if (conflictingEnchantment in missingEnchantments) {
									missingEnchantments = missingEnchantments.minus(conflictingEnchantment)
								}
							}
						}
					}
				}
			}
		}

		if (missingEnchantments.isEmpty()) return
		// Add All Missing Enchantments to tooltip. Wrapping lines at maxTooltipWidth
		val enchantmentLines = mutableListOf<Component>()
		enchantmentLines.add(Component.literal(""))
		enchantmentLines.add(Component.literal("Missing Enchantments:").grey())

		val maxTooltipWidth = 200 // Maximum width in pixels
		var line = ""
		for (missingEnchantment in missingEnchantments) {
			val enchantText = enchantIdToText(missingEnchantment) ?: continue

			val separator = if (line.isEmpty()) "" else ", "
			val testLine = "$line$separator$enchantText"
			val testComponent = Component.literal(testLine)

			if (MC.font.width(testComponent) > maxTooltipWidth && line.isNotEmpty()) {
				enchantmentLines.add(Component.literal(line.removeColorCodes()).darkGrey())
				line = enchantText
			} else {
				line = testLine
			}
		}

		if (line.isNotEmpty()) {
			enchantmentLines.add(Component.literal(line.removeColorCodes()).darkGrey())
		}
		enchantmentLines.add(Component.literal(""))

		// Find rarity line index and insert before it
		val rarityIndex = it.lines.indexOfLast { tooltipLine ->
			val text = tooltipLine.unformattedString
			text.matches(Regex("^(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SPECIAL|VERY SPECIAL).*"))
		}

		val insertIndex = if (rarityIndex != -1) rarityIndex else it.lines.size
		it.lines.addAll(insertIndex, enchantmentLines)
	}
}

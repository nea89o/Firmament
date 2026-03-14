// Current Issues: The repo is outdated and does not support maxLevel yet
// Check status at https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/issues/2311
package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import kotlin.jvm.optionals.getOrNull
import net.minecraft.network.chat.Component
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
		val showUpgradableEnchantments by toggle("show-upgradable-enchantments") { false }
		val showConflictingEnchantments by toggle("show-conflicting-enchantments") { false }
	}

	fun enchantIdToText(enchId: String, level: Int? = null): String? {
		val skyblockId = SkyblockId("${enchId.uppercase()};${level ?: 1}")
		val displayName = RepoManager.getNEUItem(skyblockId)
			?.lore?.firstOrNull()

		return if (level == null || level <= 1) {
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
		val maxLevels: Map<String, Int> =
			RepoManager.enchantData.allEnchants?.enchantExperienceCost?.mapValues { it.value.size }?: emptyMap()

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

				// Build map of enchantment text -> max level for upgradable enchants on this Tooltip line
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

				// If any enchantments can be upgraded, rebuild the line with inline upgrades
				if (upgradeMap.isNotEmpty()) {
					val parts = lineText.split(", ")
					val newLine = Component.empty()

					parts.forEachIndexed { index, part ->
						newLine.append(Component.literal(part).withStyle(line.style))

						var wasUpgraded = false
						for ((enchText, maxLevel) in upgradeMap) {
							if (enchText in part) {
								newLine.append(Component.literal(" → ${maxLevel.toRomanNumeral()}").darkGrey())
								wasUpgraded = true
								break
							}
						}

						// Add comma separator if not last part
						if (index < parts.size - 1) {
							if (wasUpgraded) {
								newLine.append(Component.literal(", ").darkGrey())
							} else {
								newLine.append(Component.literal(", ").withStyle(line.style))
							}
						}
					}

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

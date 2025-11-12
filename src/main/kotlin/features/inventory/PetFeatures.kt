package moe.nea.firmament.features.inventory

import java.util.regex.Matcher
import org.joml.Vector2i
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.jarvis.JarvisIntegration
import moe.nea.firmament.repo.ExpLadders
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters.formatPercent
import moe.nea.firmament.util.FirmFormatters.shortFormat
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.formattedString
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblock.TabListAPI
import moe.nea.firmament.util.skyblockUUID
import moe.nea.firmament.util.titleCase
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

object PetFeatures {
	val identifier: String
		get() = "pets"

	@Config
	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val highlightEquippedPet by toggle("highlight-pet") { true }
		val petOverlay by toggle("pet-overlay") { false }
		val petOverlayHud by position("pet-overlay-hud", 80, 10) {
			Vector2i()
		}
		val petOverlayHudStyle by choice("pet-overlay-hud-style") { PetOverlayHudStyles.PLAIN_NO_BACKGROUND }
	}

	enum class PetOverlayHudStyles : StringIdentifiable {
		PLAIN_NO_BACKGROUND,
		COLOUR_NO_BACKGROUND,
		PLAIN_BACKGROUND,
		COLOUR_BACKGROUND,
		ICON_ONLY;

		override fun asString() : String {
			return name
		}
	}

	private val petMenuTitle = "Pets(?: \\([0-9]+/[0-9]+\\))?".toPattern()
	private val autopetPattern =
		"§cAutopet §eequipped your §7\\[Lvl (\\d{1,3})\\] §([fa956d])([\\w\\s]+)§e! §aVIEW RULE".toPattern()
	private val petItemPattern = "§aYour pet is now holding (§[fa956d][\\w\\s]+)§a.".toPattern()
	private val petLevelUpPattern = "§aYour §([fa956d])([\\w\\s]+) §aleveled up to level §9(\\d+)§a!".toPattern()
	private val petMap = HashMap<String, ParsedPet>()
	private var currentPetUUID: String = ""
	private var tempTabPet: ParsedPet? = null
	private var tempChatPet: ParsedPet? = null

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		// Cache pets
		petMenuTitle.useMatch(MC.screenName ?: return) {
			val stack = event.slot.stack
			if (!stack.isEmpty) cachePet(stack)
			if (stack.petData?.active == true) {
				if (currentPetUUID == "") currentPetUUID = stack.skyblockUUID.toString()
				// Highlight active pet feature
				if (!TConfig.highlightEquippedPet) return
				event.context.drawGuiTexture(
					Firmament.identifier("selected_pet_background"),
					event.slot.x, event.slot.y, 16, 16,
				)
			}
		}
	}

	private fun cachePet(stack: ItemStack) {
		// Cache information about a pet
		if (stack.skyblockUUID == null) return
		if (petMap.containsKey(stack.skyblockUUID.toString()) &&
			petMap[stack.skyblockUUID.toString()]?.isComplete == true) return

		val pet = PetParser.parsePetMenuSlot(stack) ?: return
		petMap[stack.skyblockUUID.toString()] = pet
	}

	@Subscribe
	fun onSlotClick(event: SlotClickEvent) {
		// Check for switching/removing pet manually
		petMenuTitle.useMatch(MC.screenName ?: return) {
			if (event.slot.inventory is PlayerInventory) return
			if (event.button != 0 && event.button != 1) return
			val petData = event.stack.petData ?: return
			if (petData.active == true) {
				currentPetUUID = "None"
				return
			}
			if (event.button != 0) return
			if (!petMap.containsKey(event.stack.skyblockUUID.toString())) cachePet(event.stack)
			currentPetUUID = event.stack.skyblockUUID.toString()
		}
	}

	@Subscribe
	fun onChatEvent(event: ProcessChatEvent) {
		println("^^^" + event.text.formattedString() + "$$$")
		// Handle AutoPet
		var matcher = autopetPattern.matcher(event.text.formattedString())
		if (matcher.matches()) {
			val tempMap = petMap.filter { (uuid, pet) ->
				pet.name == matcher.group(3) &&
					pet.rarity == PetParser.reversePetColourMap[matcher.group(2)] &&
					pet.level <= matcher.group(1).toInt()
			}
			if (tempMap.isNotEmpty()) {
				currentPetUUID = tempMap.keys.first()
			} else {
				tempChatPet = PetParser.parsePetChatMessage(matcher.group(3), matcher.group(2), matcher.group(1).toInt())
				currentPetUUID = ""
			}
			tempTabPet = null
			return
		}
		// Handle changing pet item
		// This is needed for when pet item can't be found in tab list
		matcher = petItemPattern.matcher(event.text.formattedString())
		if (matcher.matches()) {
			petMap[currentPetUUID]?.petItem = matcher.group(1)
			tempTabPet?.petItem = matcher.group(1)
			tempChatPet?.petItem = matcher.group(1)
			// TODO: Handle tier boost pet items if required
			//  I'm not rich enough to be able to test tier boosts
		}
		// Handle pet levelling up
		// This is needed for when pet level can't be found in tab list
		matcher = petLevelUpPattern.matcher(event.text.formattedString())
		if (matcher.matches()) {
			val tempPet =
				PetParser.parsePetChatMessage(matcher.group(2), matcher.group(1), matcher.group(3).toInt()) ?: return
			val tempMap = petMap.filter { (uuid, pet) ->
				pet.name == tempPet.name &&
					pet.rarity == tempPet.rarity &&
					pet.level <= tempPet.level
			}
			if (tempMap.isNotEmpty()) petMap[tempMap.keys.first()]?.update(tempPet)
			if (tempTabPet?.name == tempPet.name && tempTabPet?.rarity == tempPet.rarity) {
				tempTabPet?.update(tempPet)
			}
			if (tempChatPet?.name == tempPet.name && tempChatPet?.rarity == tempPet.rarity) tempChatPet?.update(tempPet)
		}
	}

	private fun renderLinesAndBackground(it: HudRenderEvent, lines: List<Text>) {
		// Render background for the hud
		if (TConfig.petOverlayHudStyle == PetOverlayHudStyles.PLAIN_BACKGROUND ||
			TConfig.petOverlayHudStyle == PetOverlayHudStyles.COLOUR_BACKGROUND) {
			var maxWidth = 0
			lines.forEach { if (MC.font.getWidth(it) > maxWidth) maxWidth = MC.font.getWidth(it.unformattedString) }
			val height = if (MC.font.fontHeight * lines.size > 32) MC.font.fontHeight * lines.size else 32
			it.context.fill(0, -3, 40 + maxWidth, height + 2, 0x80000000.toInt())
		}

		// Render text for the hud
		lines.forEachIndexed { index, line ->
			it.context.drawText(
				MC.font,
				line.copy().withColor(Formatting.GRAY),
				36,
				MC.font.fontHeight * index,
				-1,
				true
			)
		}
	}

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.petOverlay || !SBData.isOnSkyblock) return

		// Possibly handle Montezuma as a future feature? Could track how many pieces have been found etc
		// Would likely need to be a separate config toggle though since that has
		// very different usefulness/purpose to the pet hud outside of rift
		if (SBData.skyblockLocation == SkyBlockIsland.RIFT) return

		// Initial data
		var pet: ParsedPet? = null
		// Do not render the HUD if there is no pet active
		if (currentPetUUID == "None") return
		// Get active pet from cache
		if (currentPetUUID != "") pet = petMap[currentPetUUID]
		// Parse tab widget for pet data
		val tabPet = PetParser.parseTabWidget(TabListAPI.getWidgetLines(TabListAPI.WidgetName.PET))
		if (pet == null && tabPet == null && tempTabPet == null && tempChatPet == null) {
			// No data on current pet
			it.context.matrices.pushMatrix()
			TConfig.petOverlayHud.applyTransformations(JarvisIntegration.jarvis, it.context.matrices)
			val lines = mutableListOf<Text>()
			lines.add(Text.literal("" + Formatting.WHITE + "Unknown Pet"))
			lines.add(Text.literal("Open Pets Menu To Fix"))
			renderLinesAndBackground(it, lines)
			it.context.matrices.popMatrix()
			return
		}
		if (pet == null) {
			// Pet is only known through tab widget or chat message, potentially saved from tab widget elsewhere
			// (e.g. another server or before removing the widget from the tab list)
			pet = tabPet ?: tempTabPet ?: tempChatPet ?: return
			if (tempTabPet == null) tempTabPet = tabPet
		}

		// Update pet based on tab widget if needed
		if (tabPet != null && pet.name == tabPet.name && pet.rarity == tabPet.rarity) {
			if (tabPet.level > pet.level) {
				// Level has increased since caching
				pet.level = tabPet.level
				pet.currentExp = tabPet.currentExp
				pet.expForNextLevel = tabPet.expForNextLevel
				pet.totalExp = tabPet.totalExp
			} else if (tabPet.currentExp > pet.currentExp) {
				// Exp has increased since caching, level has not
				pet.currentExp = tabPet.currentExp
				pet.totalExp = tabPet.totalExp
			}
			if (tabPet.petItem != pet.petItem && tabPet.petItem != "Unknown") {
				// Pet item has changed since caching
				pet.petItem = tabPet.petItem
				pet.petItemStack = tabPet.petItemStack
			}
		}

		// Set the text for the HUD

		val lines = mutableListOf<Text>()

		if (TConfig.petOverlayHudStyle == PetOverlayHudStyles.COLOUR_NO_BACKGROUND ||
			TConfig.petOverlayHudStyle == PetOverlayHudStyles.COLOUR_BACKGROUND) {
			// Colour Style
			lines.add(Text.literal("[Lvl ${pet.level}] ").append(Text.literal(pet.name)
				.withColor((Rarity.colourMap[pet.rarity]) ?: Formatting.WHITE)))

			lines.add(Text.literal(pet.petItem))
			if (pet.level != pet.maxLevel) {
				// Exp data
				lines.add(
					Text.literal(
						"" + Formatting.YELLOW + "Required L${pet.level + 1}: ${shortFormat(pet.currentExp)}" +
							Formatting.GOLD + "/" + Formatting.YELLOW +
							"${shortFormat(pet.expForNextLevel)} " + Formatting.GOLD +
							"(${formatPercent(pet.currentExp / pet.expForNextLevel)})"
					)
				)
				lines.add(
					Text.literal(
						"" + Formatting.YELLOW + "Required L100: ${shortFormat(pet.totalExp)}" +
							Formatting.GOLD + "/" + Formatting.YELLOW +
							"${shortFormat(pet.expForMax)} " + Formatting.GOLD +
							"(${formatPercent(pet.totalExp / pet.expForMax)})"
					)
				)
			} else {
				// Overflow Exp data
				lines.add(Text.literal(
					"" + Formatting.AQUA + Formatting.BOLD + "MAX LEVEL"
				))
				lines.add(Text.literal(
					"" + Formatting.GOLD + "+" + Formatting.YELLOW + "${shortFormat(pet.overflowExp)} XP"
				))
			}
		} else if (TConfig.petOverlayHudStyle == PetOverlayHudStyles.PLAIN_NO_BACKGROUND ||
			TConfig.petOverlayHudStyle == PetOverlayHudStyles.PLAIN_BACKGROUND) {
			// Plain Style
			lines.add(Text.literal("[Lvl ${pet.level}] ").append(Text.literal(pet.name)
				.withColor((Rarity.colourMap[pet.rarity]) ?: Formatting.WHITE)))

			lines.add(Text.literal(if (pet.petItem != "None" && pet.petItem != "Unknown")
				pet.petItem.substring(2) else pet.petItem))
			if (pet.level != pet.maxLevel) {
				// Exp data
				lines.add(
					Text.literal(
						"Required L${pet.level + 1}: ${shortFormat(pet.currentExp)}/" +
							"${shortFormat(pet.expForNextLevel)} " +
							"(${formatPercent(pet.currentExp / pet.expForNextLevel)})"
					)
				)
				lines.add(
					Text.literal(
						"Required L100: ${shortFormat(pet.totalExp)}/${shortFormat(pet.expForMax)} " +
							"(${formatPercent(pet.totalExp / pet.expForMax)})"
					)
				)
			} else {
				// Overflow Exp data
				lines.add(Text.literal(
					"MAX LEVEL"
				))
				lines.add(Text.literal(
					"+${shortFormat(pet.overflowExp)} XP"
				))
			}
		}

		// Render HUD

		it.context.matrices.pushMatrix()
		TConfig.petOverlayHud.applyTransformations(JarvisIntegration.jarvis, it.context.matrices)

		renderLinesAndBackground(it, lines)

		// Draw the ItemStack
		it.context.matrices.pushMatrix()
		it.context.matrices.translate(-0.5F, -0.5F)
		it.context.matrices.scale(2f, 2f)
		it.context.drawItem(pet.petItemStack.value, 0, 0)
		it.context.matrices.popMatrix()

		it.context.matrices.popMatrix()
	}
}

object PetParser {
	private val petNamePattern = " §7\\[Lvl (\\d{1,3})] §([fa956d])([\\w\\s]+)".toPattern()
	private val petItemPattern = " (§[fa956dbc4][\\s\\w]+)".toPattern()
	private val petExpPattern = " §e((?:\\d{1,3}[,.]?)+\\d*[kM]?)§6\\/§e((?:\\d{1,3}[,.]?)+\\d*[kM]?) XP §6\\(\\d+(?:.\\d+)?%\\)".toPattern()
	private val petOverflowExpPattern = " §6\\+§e((?:\\d{1,3}[,.])+\\d*[kM]?) XP".toPattern()
	private val katPattern = " Kat:.*".toPattern()

	val reversePetColourMap = mapOf(
		"f" to Rarity.COMMON,
		"a" to Rarity.UNCOMMON,
		"9" to Rarity.RARE,
		"5" to Rarity.EPIC,
		"6" to Rarity.LEGENDARY,
		"d" to Rarity.MYTHIC
	)

	val found = HashMap<String, Matcher>()

	@OptIn(ExpensiveItemCacheApi::class)
	fun parsePetChatMessage(name: String, rarityCode: String, level: Int) : ParsedPet? {
		val petId = name.uppercase().replace(" ", "_")
		val petRarity = reversePetColourMap[rarityCode] ?: Rarity.COMMON

		val neuRarity = petRarity.neuRepoRarity ?: return null
		val expLadder = ExpLadders.getExpLadder(petId, neuRarity)

		var currentExp = 0.0
		val expForNextLevel: Double
		if (found.containsKey("exp")) {
			currentExp = parseShortNumber(found.getValue("exp").group(1))
			expForNextLevel = parseShortNumber(found.getValue("exp").group(2))
		} else {
			expForNextLevel = expLadder.getPetExpForLevel(level + 1).toDouble() -
				expLadder.getPetExpForLevel(level).toDouble()
		}

		val totalExpBeforeLevel = expLadder.getPetExpForLevel(level).toDouble()
		val totalExp = totalExpBeforeLevel + currentExp
		val maxLevel = RepoManager.neuRepo.constants.petLevelingData.petLevelingBehaviourOverrides[petId]?.maxLevel ?: 100
		val expForMax = expLadder.getPetExpForLevel(maxLevel).toDouble()
		val petItemStack = lazy { RepoManager.neuRepo.items.items[petId + ";" + petRarity.ordinal].asItemStack() }

		return ParsedPet(
			name,
			petRarity,
			level,
			-1,
			expLadder,
			currentExp,
			expForNextLevel,
			totalExp,
			totalExpBeforeLevel,
			expForMax,
			0.0,
			"Unknown",
			petItemStack,
			false
		)
	}

	@OptIn(ExpensiveItemCacheApi::class)
	fun parseTabWidget(lines: List<Text>): ParsedPet? {
		found.clear()
		for (line in lines.reversed()) {
			if (!found.containsKey("kat")) {
				val matcher = katPattern.matcher(line.formattedString())
				if (matcher.matches()) {
					found["kat"] = matcher
					continue
				}
			}
			if (!found.containsKey("exp")) {
				val matcher = petExpPattern.matcher(line.formattedString())
				if (matcher.matches()) {
					found["exp"] = matcher
					continue
				}
			}
			if (!found.containsKey("exp")) {
				val matcher = petOverflowExpPattern.matcher(line.formattedString())
				if (matcher.matches()) {
					found["overflow"] = matcher
					continue
				}
			}
			if (!found.containsKey("item")) {
				val matcher = petItemPattern.matcher(line.formattedString())
				if (matcher.matches()) {
					found["item"] = matcher
					continue
				}
			}
			if (!found.containsKey("name")) {
				val matcher = petNamePattern.matcher(line.formattedString())
				if (matcher.matches()) {
					found["name"] = matcher
					continue
				}
			}
		}
		if (!found.containsKey("name")) return null

		val petName = titleCase(found.getValue("name").group(3))
		val petRarity = reversePetColourMap.getValue(found.getValue("name").group(2))
		val petId = petName.uppercase().replace(" ", "_")

		val petLevel = found.getValue("name").group(1).toInt()

		val neuRarity = petRarity.neuRepoRarity ?: return null
		val expLadder = ExpLadders.getExpLadder(petId, neuRarity)

		var currentExp = 0.0
		val expForNextLevel: Double
		if (found.containsKey("exp")) {
			currentExp = parseShortNumber(found.getValue("exp").group(1))
			expForNextLevel = parseShortNumber(found.getValue("exp").group(2))
		} else {
			expForNextLevel = expLadder.getPetExpForLevel(petLevel + 1).toDouble() -
				expLadder.getPetExpForLevel(petLevel).toDouble()
		}

		val overflowExp: Double = if (found.containsKey("overflow"))
			parseShortNumber(found.getValue("overflow").group(1)) else 0.0

		val totalExpBeforeLevel = expLadder.getPetExpForLevel(petLevel).toDouble()
		val totalExp = totalExpBeforeLevel + currentExp
		val maxLevel = RepoManager.neuRepo.constants.petLevelingData.petLevelingBehaviourOverrides[petId]?.maxLevel ?: 100
		val expForMax = expLadder.getPetExpForLevel(maxLevel).toDouble()
		val petItemStack = lazy { RepoManager.neuRepo.items.items[petId + ";" + petRarity.ordinal].asItemStack() }


		var petItem = "Unknown"
		if (found.containsKey("item")) {
			petItem = found.getValue("item").group(1)
		}

		return ParsedPet(
			petName,
			petRarity,
			petLevel,
			maxLevel,
			expLadder,
			currentExp,
			expForNextLevel,
			totalExp,
			totalExpBeforeLevel,
			expForMax,
			overflowExp,
			petItem,
			petItemStack,
			false
		)
	}

	fun parsePetMenuSlot(stack: ItemStack) : ParsedPet? {
		val petData = stack.petData ?: return null
		val expData = petData.level
		val overflow = if (expData.expTotal - expData.expRequiredForMaxLevel > 0)
			(expData.expTotal - expData.expRequiredForMaxLevel).toDouble() else 0.0
		val petItem = if (stack.petData?.heldItem != null)
			RepoManager.neuRepo.items.items.getValue(stack.petData?.heldItem).displayName else "None"
		return ParsedPet(
			titleCase(petData.type),
			Rarity.fromNeuRepo(petData.tier) ?: Rarity.COMMON,
			expData.currentLevel,
			expData.maxLevel,
			ExpLadders.getExpLadder(petData.skyblockId.toString(), petData.tier),
			expData.expInCurrentLevel.toDouble(),
			expData.expRequiredForNextLevel.toDouble(),
			expData.expTotal.toDouble(),
			expData.expTotal.toDouble() - expData.expInCurrentLevel.toDouble(),
			expData.expRequiredForMaxLevel.toDouble(),
			overflow,
			petItem,
			lazy { stack },
			true
		)
	}
}

data class ParsedPet(
	val name: String,
	val rarity: Rarity,
	var level: Int,
	val maxLevel: Int,
	val expLadder: ExpLadders.ExpLadder?,
	var currentExp: Double,
	var expForNextLevel: Double,
	var totalExp: Double,
	var totalExpBeforeLevel: Double,
	val expForMax: Double,
	var overflowExp: Double,
	var petItem: String,
	var petItemStack: Lazy<ItemStack>,
	var isComplete: Boolean
) {
	fun update(other: ParsedPet) {
		// Update the pet data to reflect another instance (of itself)
		if (other.level > level) {
			level = other.level
			currentExp = other.currentExp
			expForNextLevel = other.expForNextLevel
			totalExp = other.totalExp
			totalExpBeforeLevel = other.totalExpBeforeLevel
			overflowExp = other.overflowExp
		} else {
			if (other.currentExp > currentExp) currentExp = other.currentExp
			expForNextLevel = other.expForNextLevel
			if (other.totalExp > totalExp) totalExp = other.totalExp
			if (other.totalExpBeforeLevel > totalExpBeforeLevel) totalExpBeforeLevel = other.totalExpBeforeLevel
			if (other.overflowExp > overflowExp) overflowExp = other.overflowExp
		}
		if (other.petItem != "Unknown") petItem = other.petItem
		isComplete = false
	}
}

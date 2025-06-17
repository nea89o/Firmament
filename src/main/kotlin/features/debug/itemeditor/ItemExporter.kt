package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.concurrent.thread
import kotlin.io.path.createParentDirectories
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import net.minecraft.util.Unit
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.repo.RepoDownloadManager
import moe.nea.firmament.util.HypixelPetInfo
import moe.nea.firmament.util.LegacyTagWriter.Companion.toLegacyString
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.getLegacyFormatString
import moe.nea.firmament.util.json.toJsonArray
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.mc.toNbtList
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.transformEachRecursively
import moe.nea.firmament.util.unformattedString

class ItemExporter(var itemStack: ItemStack) {
	var lore = itemStack.loreAccordingToNbt
	var name = itemStack.displayNameAccordingToNbt
	val extraAttribs = itemStack.extraAttributes.copy()
	val legacyNbt = NbtCompound()
	val warnings = mutableListOf<String>()

	fun preprocess() {
		// TODO: split up preprocess steps into preprocess actions that can be toggled in a ui
		extraAttribs.remove("timestamp")
		extraAttribs.remove("uuid")
		extraAttribs.remove("modifier")
		extraAttribs.getString("petInfo").ifPresent { petInfoJson ->
			var petInfo = Firmament.json.decodeFromString<HypixelPetInfo>(petInfoJson)
			petInfo = petInfo.copy(candyUsed = 0, heldItem = null, exp = 0.0, active = null, uuid = null)
			extraAttribs.putString("petInfo", Firmament.tightJson.encodeToString(petInfo))
		}
		itemStack.skyBlockId?.let {
			extraAttribs.putString("id", it.neuItem)
		}
		trimLore()
	}

	fun trimLore() {
		val rarityIdx = lore.indexOfLast {
			val firstWordInLine = it.unformattedString.words().filter { it.length > 2 }.firstOrNull()
			firstWordInLine?.let(Rarity::fromString) != null
		}
		if (rarityIdx >= 0) {
			lore = lore.subList(0, rarityIdx + 1)
		}
		deleteLineUntilNextSpace { it.startsWith("Held Item: ") }
		deleteLineUntilNextSpace { it.startsWith("Progress to Level ") }
		deleteLineUntilNextSpace { it.startsWith("MAX LEVEL") }
		collapseWhitespaces()

		name = name.transformEachRecursively {
			var string = it.directLiteralStringContent ?: return@transformEachRecursively it
			string = string.replace("Lvl \\d+".toRegex(), "Lvl {LVL}")
			Text.literal(string).setStyle(it.style)
		}
	}

	fun collapseWhitespaces() {
		lore = (listOf(null as Text?) + lore).zipWithNext()
			.filter { !it.first?.unformattedString.isNullOrBlank() || !it.second?.unformattedString.isNullOrBlank() }
			.map { it.second!! }
	}

	fun deleteLineUntilNextSpace(search: (String) -> Boolean) {
		val idx = lore.indexOfFirst { search(it.unformattedString) }
		if (idx < 0) return
		val l = lore.toMutableList()
		val p = l.subList(idx, l.size)
		val nextBlank = p.indexOfFirst { it.unformattedString.isEmpty() }
		if (nextBlank < 0)
			p.clear()
		else
			p.subList(0, nextBlank).clear()
		lore = l
	}

	fun processNbt() {
		// TODO: calculate hideflags
		legacyNbt.put("HideFlags", NbtInt.of(254))
		copyUnbreakable()
		copyExtraAttributes()
		copyLegacySkullNbt()
		copyDisplay()
		copyEnchantments()
		copyEnchantGlint()
		// TODO: copyDisplay
	}

	private fun copyDisplay() {
		legacyNbt.put("display", NbtCompound().apply {
			put("Lore", lore.map { NbtString.of(it.getLegacyFormatString(trimmed = true)) }.toNbtList())
			putString("Name", name.getLegacyFormatString(trimmed = true))
		})
	}

	fun exportJson(): JsonElement {
		preprocess()
		processNbt()
		return buildJsonObject {
			val (itemId, damage) = legacyifyItemStack()
			put("itemid", itemId)
			put("displayname", name.getLegacyFormatString(trimmed = true))
			put("nbttag", legacyNbt.toLegacyString())
			put("damage", damage)
			put("lore", lore.map { it.getLegacyFormatString(trimmed = true) }.toJsonArray())
			val sbId = itemStack.skyBlockId
			if (sbId == null)
				warnings.add("Could not find skyblock id")
			put("internalname", sbId?.neuItem)
			put("clickcommand", "")
			put("crafttext", "")
			put("modver", "Firmament ${Firmament.version.friendlyString}")
			put("infoType", "")
			put("info", JsonArray(listOf()))
		}

	}

	companion object {
		@Subscribe
		fun load(event: ClientStartedEvent) {
			thread(start = true, name = "ItemExporter Meta Load Thread") {
				LegacyItemData.itemLut
			}
		}

		@Subscribe
		fun onKeyBind(event: HandledScreenKeyPressedEvent) {
			if (event.matches(PowerUserTools.TConfig.exportItemStackToRepo)) {
				val itemStack = event.screen.focusedItemStack ?: return
				val exporter = ItemExporter(itemStack)
				val json = exporter.exportJson()
				val jsonFormatted = Firmament.twoSpaceJson.encodeToString(json)
				val itemFile = RepoDownloadManager.repoSavedLocation.resolve("items")
					.resolve("${json.jsonObject["internalname"]!!.jsonPrimitive.content}.json")
				itemFile.createParentDirectories()
				itemFile.writeText(jsonFormatted)
				PowerUserTools.lastCopiedStack = Pair(
					itemStack,
					tr(
						"firmament.repoexport.success",
						"Exported item to ${itemFile.relativeTo(RepoDownloadManager.repoSavedLocation)}${
							exporter.warnings.joinToString(
								""
							) { "\nWarning: $it" }
						}"
					)
				)
			}
		}
	}

	fun copyEnchantGlint() {
		if (itemStack.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) == true) {
			val ench = legacyNbt.getListOrEmpty("ench")
			legacyNbt.put("ench", ench)
		}
	}

	private fun copyUnbreakable() {
		if (itemStack.get(DataComponentTypes.UNBREAKABLE) == Unit.INSTANCE) {
			legacyNbt.putBoolean("Unbreakable", true)
		}
	}

	fun copyEnchantments() {
		val enchantments = itemStack.get(DataComponentTypes.ENCHANTMENTS)?.takeIf { !it.isEmpty } ?: return
		val enchTag = legacyNbt.getListOrEmpty("ench")
		legacyNbt.put("ench", enchTag)
		enchantments.enchantmentEntries.forEach { entry ->
			val id = entry.key.key.get().value
			val legacyId = LegacyItemData.enchantmentLut[id]
			if (legacyId == null) {
				warnings.add("Could not find legacy enchantment id for ${id}")
				return@forEach
			}
			enchTag.add(NbtCompound().apply {
				putShort("lvl", entry.intValue.toShort())
				putShort(
					"id",
					legacyId.id.toShort()
				)
			})
		}
	}

	fun copyExtraAttributes() {
		legacyNbt.put("ExtraAttributes", extraAttribs)
	}

	fun copyLegacySkullNbt() {
		val profile = itemStack.get(DataComponentTypes.PROFILE) ?: return
		legacyNbt.put("SkullOwner", NbtCompound().apply {
			profile.id.ifPresent {
				putString("Id", it.toString())
			}
			putBoolean("hypixelPopulated", true)
			put("Properties", NbtCompound().apply {
				profile.properties().forEach { prop, value ->
					val list = getListOrEmpty(prop)
					put(prop, list)
					list.add(NbtCompound().apply {
						value.signature?.let {
							putString("Signature", it)
						}
						putString("Value", value.value)
						putString("Name", value.name)
					})
				}
			})
		})
	}

	fun legacyifyItemStack(): LegacyItemData.LegacyItemType {
		// TODO: add a default here
		return LegacyItemData.itemLut[itemStack.item]!!
	}
}

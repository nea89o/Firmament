package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrNull
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.tags.ItemTags
import net.minecraft.network.chat.Component
import net.minecraft.util.Unit
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.HypixelPetInfo
import moe.nea.firmament.util.LegacyTagWriter.Companion.toLegacyString
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.getLegacyFormatString
import moe.nea.firmament.util.json.toJsonArray
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.mc.toNbtList
import moe.nea.firmament.util.modifyExtraAttributes
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.transformEachRecursively
import moe.nea.firmament.util.unformattedString

class LegacyItemExporter private constructor(var itemStack: ItemStack) {
	init {
		require(!itemStack.isEmpty)
		itemStack.count = 1
	}

	var lore = itemStack.loreAccordingToNbt
	val originalId = itemStack.extraAttributes.getString("id")
	var name = itemStack.displayNameAccordingToNbt
	val extraAttribs = itemStack.extraAttributes.copy()
	val legacyNbt = CompoundTag()
	val warnings = mutableListOf<String>()

	// TODO: check if lore contains non 1.8.9 able hex codes and emit lore in overlay files if so

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
		itemStack.loreAccordingToNbt = itemStack.item.defaultInstance.loreAccordingToNbt
		itemStack.remove(DataComponents.CUSTOM_NAME)
	}

	fun trimLore() {
		val rarityIdx = lore.indexOfLast {
			val firstWordInLine = it.unformattedString.words().filter { it.length > 2 }.firstOrNull()
			firstWordInLine?.let(Rarity::fromString) != null
		}
		if (rarityIdx >= 0) {
			lore = lore.subList(0, rarityIdx + 1)
		}

		trimStats()

		deleteLineUntilNextSpace { it.startsWith("Held Item: ") }
		deleteLineUntilNextSpace { it.startsWith("Progress to Level ") }
		deleteLineUntilNextSpace { it.startsWith("MAX LEVEL") }
		deleteLineUntilNextSpace { it.startsWith("Click to view recipe!") }
		collapseWhitespaces()

		name = name.transformEachRecursively {
			var string = it.directLiteralStringContent ?: return@transformEachRecursively it
			string = string.replace("Lvl \\d+".toRegex(), "Lvl {LVL}")
			Component.literal(string).setStyle(it.style)
		}

		if (lore.isEmpty())
			lore = listOf(Component.empty())
	}

	private fun trimStats() {
		val lore = this.lore.toMutableList()
		for (index in lore.indices) {
			val value = lore[index]
			val statLine = SBItemStack.parseStatLine(value)
			if (statLine == null) break
			val v = value.copy()
			require(value.directLiteralStringContent == "")
			v.siblings.removeIf { it.directLiteralStringContent!!.contains("(") }
			val last = v.siblings.last()
			v.siblings[v.siblings.lastIndex] =
				Component.literal(last.directLiteralStringContent!!.trimEnd())
					.setStyle(last.style)
			lore[index] = v
		}
		this.lore = lore
	}

	fun collapseWhitespaces() {
		lore = (listOf(null as Component?) + lore).zipWithNext()
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
		legacyNbt.put("HideFlags", IntTag.valueOf(254))
		copyUnbreakable()
		copyItemModel()
		copyPotion()
		copyExtraAttributes()
		copyLegacySkullNbt()
		copyDisplay()
		copyColour()
		copyEnchantments()
		copyEnchantGlint()
		// TODO: copyDisplay
	}

	private fun copyPotion() {
		val effects = itemStack.get(DataComponents.POTION_CONTENTS) ?: return
		legacyNbt.put("CustomPotionEffects", ListTag().also {
			effects.allEffects.forEach { effect ->
				val effectId = effect.effect.unwrapKey().get().identifier().path
				val duration = effect.duration
				val legacyId = LegacyItemData.effectList[effectId]!!

				it.add(CompoundTag().apply {
					put("Ambient", ByteTag.valueOf(false))
					put("Duration", IntTag.valueOf(duration))
					put("Id", ByteTag.valueOf(legacyId.id.toByte()))
					put("Amplifier", ByteTag.valueOf(effect.amplifier.toByte()))
				})
			}
		})
	}

	fun CompoundTag.getOrPutCompound(name: String): CompoundTag {
		val compound = getCompoundOrEmpty(name)
		put(name, compound)
		return compound
	}

	private fun copyColour() {
		if (!itemStack.`is`(ItemTags.DYEABLE)) {
			itemStack.remove(DataComponents.DYED_COLOR)
			return
		}
		val leatherTint = itemStack.componentsPatch.get(DataComponents.DYED_COLOR)?.getOrNull() ?: return
		legacyNbt.getOrPutCompound("display").put("color", IntTag.valueOf(leatherTint.rgb))
	}

	private fun copyItemModel() {
		val itemModel = itemStack.get(DataComponents.ITEM_MODEL) ?: return
		legacyNbt.put("ItemModel", StringTag.valueOf(itemModel.toString()))
	}

	private fun copyDisplay() {
		legacyNbt.getOrPutCompound("display").apply {
			put("Lore", lore.map { StringTag.valueOf(it.getLegacyFormatString(trimmed = true)) }.toNbtList())
			putString("Name", name.getLegacyFormatString(trimmed = true))
		}
	}

	fun exportModernSnbt(): Tag {
		val overlay = ItemStack.CODEC.encodeStart(MC.currentOrDefaultRegistryNbtOps, itemStack.copy().also {
			it.modifyExtraAttributes { attribs ->
				originalId.ifPresent { attribs.putString("id", it) }
			}
		}).orThrow
		val overlayWithVersion =
			ExportedTestConstantMeta.SOURCE_CODEC.encode(ExportedTestConstantMeta.current, NbtOps.INSTANCE, overlay)
				.orThrow
		return overlayWithVersion
	}

	fun prepare() {
		preprocess()
		processNbt()
		itemStack.extraAttributes = extraAttribs
	}

	fun exportJson(): JsonElement {
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
		fun createExporter(itemStack: ItemStack): LegacyItemExporter {
			return LegacyItemExporter(itemStack.copy()).also { it.prepare() }
		}

		@Subscribe
		fun load(event: ClientStartedEvent) {
			thread(start = true, name = "ItemExporter Meta Load Thread") {
				LegacyItemData.itemLut
			}
		}
	}

	fun copyEnchantGlint() {
		if (itemStack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) == true) {
			val ench = legacyNbt.getListOrEmpty("ench")
			legacyNbt.put("ench", ench)
		}
	}

	private fun copyUnbreakable() {
		if (itemStack.get(DataComponents.UNBREAKABLE) == Unit.INSTANCE) {
			legacyNbt.putBoolean("Unbreakable", true)
		}
	}

	fun copyEnchantments() {
		val enchantments = itemStack.get(DataComponents.ENCHANTMENTS)?.takeIf { !it.isEmpty } ?: return
		val enchTag = legacyNbt.getListOrEmpty("ench")
		legacyNbt.put("ench", enchTag)
		enchantments.entrySet().forEach { entry ->
			val id = entry.key.unwrapKey().get().identifier()
			val legacyId = LegacyItemData.enchantmentLut[id]
			if (legacyId == null) {
				warnings.add("Could not find legacy enchantment id for ${id}")
				return@forEach
			}
			enchTag.add(CompoundTag().apply {
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
		val profile = itemStack.get(DataComponents.PROFILE) ?: return
		legacyNbt.put("SkullOwner", CompoundTag().apply {
			putString("Id", profile.partialProfile().id.toString())
			putBoolean("hypixelPopulated", true)
			put("Properties", CompoundTag().apply {
				profile.partialProfile().properties().forEach { prop, value ->
					val list = getListOrEmpty(prop)
					put(prop, list)
					list.add(CompoundTag().apply {
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
		if (itemStack.item == Items.LINGERING_POTION || itemStack.item == Items.SPLASH_POTION)
			return LegacyItemData.LegacyItemType("potion", 16384)
		return LegacyItemData.itemLut[itemStack.item]!!
	}
}

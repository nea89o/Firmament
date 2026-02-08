package moe.nea.firmament.features.debug.itemeditor

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.util.StringUtil.camelWords
import moe.nea.firmament.util.mc.loadItemFromNbt

/**
 * Load data based on [prismarine.js' 1.8 item data](https://github.com/PrismarineJS/minecraft-data/blob/master/data/pc/1.8/items.json)
 */
object LegacyItemData {
	@Serializable
	data class ItemData(
		val id: Int,
		val name: String,
		val displayName: String,
		val stackSize: Int,
		val variations: List<Variation> = listOf()
	) {
		val properId = if (name.contains(":")) name else "minecraft:$name"

		fun allVariants() =
			variations.map { LegacyItemType(properId, it.metadata.toShort()) } + LegacyItemType(properId, 0)
	}

	@Serializable
	data class Variation(
		val metadata: Int, val displayName: String
	)

	data class LegacyItemType(
		val name: String,
		val metadata: Short
	) {
		override fun toString(): String {
			return "$name:$metadata"
		}
	}

	@Serializable
	data class EnchantmentData(
		val id: Int,
		val name: String,
		val displayName: String,
	)

	inline fun <reified T : Any> getLegacyData(name: String) =
		Firmament.tryDecodeJsonFromStream<T>(
			LegacyItemData::class.java.getResourceAsStream("/legacy_data/$name.json")!!
		).getOrThrow()

	val enchantmentData = getLegacyData<List<EnchantmentData>>("enchantments")
	val enchantmentLut = enchantmentData.associateBy { Identifier.withDefaultNamespace(it.name) }

	val itemDat = getLegacyData<List<ItemData>>("items")

	@OptIn(ExpensiveItemCacheApi::class) // This is fine, we get loaded in a thread.
	val itemLut = itemDat.flatMap { item ->
		item.allVariants().map { legacyItemType ->
			val nbt = ItemCache.convert189ToModern(CompoundTag().apply {
				putString("id", legacyItemType.name)
				putByte("Count", 1)
				putShort("Damage", legacyItemType.metadata)
			})!!
			nbt.remove("components")
			val stack = loadItemFromNbt(nbt) ?: error("Could not transform $legacyItemType: $nbt")
			stack.item to legacyItemType
		}
	}.toMap()

	@Serializable
	data class LegacyEffect(
		val id: Int,
		val name: String,
		val displayName: String,
		val type: String
	)

	val effectList = getLegacyData<List<LegacyEffect>>("effects")
		.associateBy {
			it.name.camelWords().map { it.trim().lowercase() }.joinToString("_")
		}
}

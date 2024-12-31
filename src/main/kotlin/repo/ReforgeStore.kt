package moe.nea.firmament.repo

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepoFile
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import io.github.moulberry.repo.data.NEURecipe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKey
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.json.KJsonOps
import moe.nea.firmament.util.skyblock.ItemType

object ReforgeStore : ExtraRecipeProvider, IReloadable {
	override fun provideExtraRecipes(): Iterable<NEURecipe> {
		return emptyList()
	}

	var byType: Map<ItemType, List<Reforge>> = mapOf()
	var byVanilla: Map<RegistryKey<Item>, List<Reforge>> = mapOf()
	var byInternalName: Map<SkyblockId, List<Reforge>> = mapOf()
	var modifierLut = mapOf<ReforgeId, Reforge>()
	var byReforgeStone = mapOf<SkyblockId, Reforge>()
	var allReforges = listOf<Reforge>()

	fun findEligibleForItem(itemType: ItemType): List<Reforge> {
		return byType[itemType] ?: listOf()
	}

	fun findEligibleForInternalName(internalName: SkyblockId): List<Reforge> {
		return byInternalName[internalName] ?: listOf()
	}

	//TODO: return byVanillla
	override fun reload(repo: NEURepository) {
		val basicReforges =
			repo.file("constants/reforges.json")
				?.kJson(serializer<Map<String, Reforge>>())
				?.values ?: emptyList()
		val advancedReforges =
			repo.file("constants/reforgestones.json")
				?.kJson(serializer<Map<String, Reforge>>())
				?.values ?: emptyList()
		val allReforges = (basicReforges + advancedReforges)
		modifierLut = allReforges.associateBy { it.reforgeId }
		byReforgeStone = allReforges.filter { it.reforgeStone != null }
			.associateBy { it.reforgeStone!! }
		val byType = mutableMapOf<ItemType, MutableList<Reforge>>()
		val byVanilla = mutableMapOf<RegistryKey<Item>, MutableList<Reforge>>()
		val byInternalName = mutableMapOf<SkyblockId, MutableList<Reforge>>()
		this.byType = byType
		this.byVanilla = byVanilla
		this.byInternalName = byInternalName
		for (reforge in allReforges) {
			for (eligibleItem in reforge.eligibleItems) {
				when (eligibleItem) {
					is Reforge.ReforgeEligibilityFilter.AllowsInternalName -> {
						byInternalName.getOrPut(eligibleItem.internalName, ::mutableListOf).add(reforge)
					}

					is Reforge.ReforgeEligibilityFilter.AllowsItemType -> {
						val actualItemTypes = resolveItemType(eligibleItem.itemType)
						for (itemType in actualItemTypes) {
							byType.getOrPut(itemType, ::mutableListOf).add(reforge)
							byType.getOrPut(itemType.dungeonVariant, ::mutableListOf).add(reforge)
						}
					}

					is Reforge.ReforgeEligibilityFilter.AllowsVanillaItemType -> {
						byVanilla.getOrPut(eligibleItem.minecraftId, ::mutableListOf).add(reforge)
					}
				}
			}
		}
		this.allReforges = allReforges
	}

	fun resolveItemType(itemType: ItemType): List<ItemType> {
		if (ItemType.SWORD == itemType) {
			return listOf(
				ItemType.SWORD,
				ItemType.GAUNTLET,
				ItemType.LONGSWORD,// TODO: check name
				ItemType.FISHING_WEAPON,// TODO: check name
			)
		}
		if (itemType == ItemType.ofName("ARMOR")) {
			return listOf(
				ItemType.CHESTPLATE,
				ItemType.LEGGINGS,
				ItemType.HELMET,
				ItemType.BOOTS,
			)
		}
		if (itemType == ItemType.EQUIPMENT) {
			return listOf(
				ItemType.CLOAK,
				ItemType.BRACELET,
				ItemType.NECKLACE,
				ItemType.BELT,
				ItemType.GLOVES,
			)
		}
		if (itemType == ItemType.ROD) {
			return listOf(ItemType.FISHING_ROD, ItemType.FISHING_WEAPON)
		}
		return listOf(itemType)
	}

	fun <T> NEURepoFile.kJson(serializer: KSerializer<T>): T {
		val rawJson = json(JsonElement::class.java)
		try {
			val kJsonElement = JsonOps.INSTANCE.convertTo(KJsonOps.INSTANCE, rawJson)
			return Firmament.json.decodeFromJsonElement(serializer, kJsonElement)
		} catch (ex: Exception) {
			throw NEURepositoryException(path, "Could not decode kotlin JSON element", ex)
		}
	}
}

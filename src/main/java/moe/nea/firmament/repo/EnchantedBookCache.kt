package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.skyblock.EnchantedBookLore
import moe.nea.firmament.util.skyblockId

class EnchantedBookCache : IReloadable {
	var byName: Map<String, SkyblockId> = mapOf()
	override fun reload(repo: NEURepository) {
		byName = repo.items.items.values
			.filter { it.displayName.endsWith("Enchanted Book") }
			.mapNotNull { item ->
				EnchantedBookLore.findEnchantmentName(item.lore)
					?.let { it.removeColorCodes() to item.skyblockId }
			}
			.toMap()
	}
}

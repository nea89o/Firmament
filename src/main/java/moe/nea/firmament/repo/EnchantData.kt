package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.constants.Enchants

class EnchantData : IReloadable {
	var allEnchants: Enchants? = null
	var enchantMaxLevels: Map<String, Int> = emptyMap()
	override fun reload(repo: NEURepository) {
		allEnchants = repo.constants.enchants
		val knownEnchants = allEnchants?.availableEnchants?.values?.flatten()?.toSet() ?: emptySet()
		enchantMaxLevels = buildMap {
			for (id in repo.items.items.keys) {
				val semicolonIdx = id.indexOf(';')
				if (semicolonIdx == -1) continue
				val name = id.substring(0, semicolonIdx).lowercase()
				if (name !in knownEnchants) continue
				val level = id.substring(semicolonIdx + 1).toIntOrNull() ?: continue
				merge(name, level, ::maxOf)
			}
		}
	}
}

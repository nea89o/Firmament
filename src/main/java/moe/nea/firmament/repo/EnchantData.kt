package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.constants.Enchants

class EnchantData : IReloadable {
	var allEnchants: Enchants? = null
	override fun reload(repo: NEURepository) {
		allEnchants = repo.constants.enchants
	}
}

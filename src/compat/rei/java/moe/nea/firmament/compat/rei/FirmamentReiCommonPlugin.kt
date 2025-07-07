package moe.nea.firmament.compat.rei

import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.plugins.REICommonPlugin
import moe.nea.firmament.repo.RepoManager

class FirmamentReiCommonPlugin : REICommonPlugin {
	override fun registerEntryTypes(registry: EntryTypeRegistry) {
		if (!RepoManager.shouldLoadREI()) return
		registry.register(FirmamentReiPlugin.SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
	}
}

package moe.nea.firmament.compat.rei

import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.plugins.REICommonPlugin

class FirmamentReiCommonPlugin : REICommonPlugin {
	override fun registerEntryTypes(registry: EntryTypeRegistry) {
		registry.register(FirmamentReiPlugin.SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
	}
}

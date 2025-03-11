package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import net.minecraft.item.ItemStack
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.skyBlockId

@AutoService(SBItemProperty::class)
object SBItemId : SBItemProperty.State<SkyblockId>() {

	override fun fromStack(stack: ItemStack, store: SBItemData): SkyblockId? {
		return stack.skyBlockId
	}

	override fun applyToStack(stack: ItemStack, store: SBItemData, value: SkyblockId?): ItemStack {
		val id = value ?: SkyblockId.NULL
		return RepoManager.getNEUItem(id).asItemStack(idHint = id).copy()
	}

	override val order: Int
		get() = -10000
}

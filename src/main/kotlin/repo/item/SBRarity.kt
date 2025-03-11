package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import io.github.moulberry.repo.data.NEUItem
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.skyblock.Rarity

@AutoService(SBItemProperty::class)
object SBRarity : SBItemProperty<Rarity>() {
	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): Rarity? {
		return Rarity.fromItem(stack)
	}

	override fun fromNeuItem(neuItem: NEUItem, store: SBItemData): Rarity? {
		return Rarity.fromStringLore(neuItem.lore)
	}
}

package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.skyblock.stats.StatBlock

@AutoService(SBItemProperty::class)
object SBItemStats : SBItemProperty<StatBlock>() {
	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): StatBlock? {
		return StatBlock.fromLore(stack.loreAccordingToNbt)
	}

	override val order: Int
		get() = 100
}

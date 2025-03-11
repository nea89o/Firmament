package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtInt
import moe.nea.firmament.repo.set
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.mc.modifyLore
import moe.nea.firmament.util.modifyExtraAttributes
import moe.nea.firmament.util.skyblock.Rarity

@AutoService(SBItemProperty::class)
object SBRecombobulator : SBItemProperty.State<Boolean>() {
	override fun applyToStack(
		stack: ItemStack,
		store: SBItemData,
		value: Boolean?
	): ItemStack {
		if (value != true) return stack
		stack.modifyLore { lore ->
			Rarity.recombobulateLore(lore)
		}
		stack.modifyExtraAttributes {
			it["rarity_upgrades"] = NbtInt.of(1)
		}
		return stack
	}

	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): Boolean? {
		return stack.extraAttributes.getInt("rarity_upgrades") > 0
	}

	override val order: Int
		get() = -100
}

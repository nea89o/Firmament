package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import io.github.moulberry.repo.data.NEUItem
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

@AutoService(SBItemProperty::class)
object SBBreakingPower : SBItemProperty<Int>() {
	private val BREAKING_POWER_REGEX = "Breaking Power (?<power>[0-9]+)".toPattern()

	fun fromLore(string: String?): Int? {
		return BREAKING_POWER_REGEX.useMatch(string) {
			group("power").toInt()
		}
	}

	override fun fromNeuItem(neuItem: NEUItem, store: SBItemData): Int? {
		return fromLore(neuItem.lore.firstOrNull()?.removeColorCodes())
	}

	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): Int? {
		return fromLore(stack.loreAccordingToNbt.firstOrNull()?.unformattedString)
	}
}

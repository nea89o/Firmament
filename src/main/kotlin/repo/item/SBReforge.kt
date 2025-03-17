package moe.nea.firmament.repo.item

import com.google.auto.service.AutoService
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.ReforgeStore
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ReforgeId
import moe.nea.firmament.util.blue
import moe.nea.firmament.util.getReforgeId
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.modifyLore
import moe.nea.firmament.util.modifyExtraAttributes
import moe.nea.firmament.util.prepend
import moe.nea.firmament.util.reconstitute
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.skyblock.stats.BuffKind
import moe.nea.firmament.util.unformattedString

@AutoService(SBItemProperty::class)
object SBReforge : SBItemProperty.State<ReforgeId>() {
	override fun applyToStack(
		stack: ItemStack,
		store: SBItemData,
		value: ReforgeId?
	): ItemStack {
		val reforgeId = value ?: return stack
		stack.modifyExtraAttributes { data ->
			data.putString("modifier", reforgeId.id)
		}
		val rarity = Rarity.fromItem(stack) ?: return stack
		val reforge = ReforgeStore.modifierLut[reforgeId] ?: return stack
		stack.displayNameAccordingToNbt = stack.displayNameAccordingToNbt.copy()
			.prepend(Text.literal(reforge.reforgeName + " ").formatted(rarity.colour()))
		val reforgeStats = reforge.reforgeStats?.get(rarity) ?: mapOf()
		SBItemStack.appendEnhancedStats(stack, reforgeStats, BuffKind.REFORGE)
		reforge.reforgeAbility?.get(rarity)?.let { reforgeAbility ->
			val formattedReforgeAbility = ItemCache.un189Lore(reforgeAbility)
				.grey()
			stack.modifyLore {
				val lastBlank = it.indexOfLast { it.unformattedString.isBlank() }
				val newList = mutableListOf<Text>()
				newList.addAll(it.subList(0, lastBlank))
				newList.add(Text.literal(""))
				newList.add(Text.literal("${reforge.reforgeName} Bonus").blue())
				MC.font.textHandler.wrapLines(formattedReforgeAbility, 180, Style.EMPTY).mapTo(newList) {
					it.reconstitute()
				}
				newList.addAll(it.subList(lastBlank, it.size))
				newList
			}
		}
		return stack
	}

	override fun fromStack(
		stack: ItemStack,
		store: SBItemData
	): ReforgeId? {
		return stack.getReforgeId()
	}
}

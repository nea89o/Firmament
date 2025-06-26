package moe.nea.firmament.util

import com.google.auto.service.AutoService
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.compatloader.CompatLoader

interface HoveredItemStackProvider {
	fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack?

	companion object : CompatLoader<HoveredItemStackProvider>(HoveredItemStackProvider::class)
}

@AutoService(HoveredItemStackProvider::class)
class VanillaScreenProvider : HoveredItemStackProvider {
	override fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack? {
		screen as AccessorHandledScreen
		val vanillaSlot = screen.focusedSlot_Firmament?.stack
		return vanillaSlot
	}
}

val HandledScreen<*>.focusedItemStack: ItemStack?
	get() =
		HoveredItemStackProvider.allValidInstances
			.firstNotNullOfOrNull { it.provideHoveredItemStack(this)?.takeIf { !it.isEmpty } }

package moe.nea.firmament.compat.rei

import com.google.auto.service.AutoService
import me.shedaniel.math.impl.PointHelper
import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.HoveredItemStackProvider
import moe.nea.firmament.util.compatloader.CompatLoader

@AutoService(HoveredItemStackProvider::class)
@CompatLoader.RequireMod("roughlyenoughitems")
class ScreenRegistryHoveredItemStackProvider : HoveredItemStackProvider {
	override fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack? {
		val entryStack = ScreenRegistry.getInstance().getFocusedStack(screen, PointHelper.ofMouse())
			?: return null
		return entryStack.value as? ItemStack ?: entryStack.cheatsAs().value
	}
}

@AutoService(HoveredItemStackProvider::class)
@CompatLoader.RequireMod("roughlyenoughitems")
class OverlayHoveredItemStackProvider : HoveredItemStackProvider {
	override fun provideHoveredItemStack(screen: HandledScreen<*>): ItemStack? {
		var baseElement: Element? = REIRuntime.getInstance().overlay.orElse(null)
		val mx = PointHelper.getMouseFloatingX()
		val my = PointHelper.getMouseFloatingY()
		while (true) {
			if (baseElement is Slot) return baseElement.currentEntry.cheatsAs().value
			if (baseElement !is ParentElement) return null
			baseElement = baseElement.hoveredElement(mx, my).orElse(null)
		}
	}
}

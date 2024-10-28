package moe.nea.firmament.features.inventory

import io.github.moulberry.repo.data.NEUCraftingRecipe
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.skyblockId

object CraftingOverlay : FirmamentFeature {

	private var screen: GenericContainerScreen? = null
	private var recipe: NEUCraftingRecipe? = null
	private var useNextScreen = false
	private val craftingOverlayIndices = listOf(
		10, 11, 12,
		19, 20, 21,
		28, 29, 30,
	)
	val CRAFTING_SCREEN_NAME = "Craft Item"

	fun setOverlay(screen: GenericContainerScreen?, recipe: NEUCraftingRecipe) {
		this.screen = screen
		if (screen == null) {
			useNextScreen = true
		}
		this.recipe = recipe
	}

	@Subscribe
	fun onScreenChange(event: ScreenChangeEvent) {
		if (useNextScreen && event.new is GenericContainerScreen
			&& event.new.title?.string == "Craft Item"
		) {
			useNextScreen = false
			screen = event.new
		}
	}

	override val identifier: String
		get() = "crafting-overlay"

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.After) {
		val slot = event.slot
		val recipe = this.recipe ?: return
		if (slot.inventory != screen?.screenHandler?.inventory) return
		val recipeIndex = craftingOverlayIndices.indexOf(slot.index)
		if (recipeIndex < 0) return
		val expectedItem = recipe.inputs[recipeIndex]
		val actualStack = slot.stack ?: ItemStack.EMPTY!!
		val actualEntry = SBItemStack(actualStack)
		if ((actualEntry.skyblockId != expectedItem.skyblockId || actualEntry.getStackSize() < expectedItem.amount)
			&& expectedItem.amount.toInt() != 0
		) {
			event.context.fill(
				event.slot.x,
				event.slot.y,
				event.slot.x + 16,
				event.slot.y + 16,
				0x80FF0000.toInt()
			)
		}
		if (!slot.hasStack()) {
			val itemStack = SBItemStack(expectedItem)?.asImmutableItemStack() ?: return
			event.context.drawItem(itemStack, event.slot.x, event.slot.y)
			event.context.drawItemInSlot(
				MC.font,
				itemStack,
				event.slot.x,
				event.slot.y,
				"${Formatting.RED}${expectedItem.amount.toInt()}"
			)
		}
	}
}

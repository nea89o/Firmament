package moe.nea.firmament.features.inventory

import io.github.moulberry.repo.data.NEUCraftingRecipe
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.ChatFormatting
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.RequiresComponents
import moe.nea.firmament.util.skyblockId

object CraftingOverlay {

	private var screen: ContainerScreen? = null
	private var recipe: NEUCraftingRecipe? = null
	private var useNextScreen = false
	private val craftingOverlayIndices = listOf(
		10, 11, 12,
		19, 20, 21,
		28, 29, 30,
	)
	val CRAFTING_SCREEN_NAME = "Craft Item"

	fun setOverlay(screen: ContainerScreen?, recipe: NEUCraftingRecipe) {
		this.screen = screen
		if (screen == null) {
			useNextScreen = true
		}
		this.recipe = recipe
	}

	@Subscribe
	fun onScreenChange(event: ScreenChangeEvent) {
		if (useNextScreen && event.new is ContainerScreen
			&& event.new.title?.string == "Craft Item"
		) {
			useNextScreen = false
			screen = event.new
		}
	}

	val identifier: String
		get() = "crafting-overlay"

	@OptIn(ExpensiveItemCacheApi::class, RequiresComponents::class)
	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.After) {
		val slot = event.slot
		val recipe = this.recipe ?: return
		if (slot.container != screen?.menu?.container) return
		val recipeIndex = craftingOverlayIndices.indexOf(slot.containerSlot)
		if (recipeIndex < 0) return
		val expectedItem = recipe.inputs[recipeIndex]
		val actualStack = slot.item ?: ItemStack.EMPTY!!
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
		if (!slot.hasItem()) {
			val itemStack = SBItemStack(expectedItem)?.asImmutableItemStack() ?: return
			event.context.item(itemStack.upgrade(), event.slot.x, event.slot.y)
			event.context.itemDecorations(
				MC.font,
				itemStack.upgrade(),
				event.slot.x,
				event.slot.y,
				"${ChatFormatting.RED}${expectedItem.amount.toInt()}"
			)
		}
	}
}

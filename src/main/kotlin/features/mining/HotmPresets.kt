package moe.nea.firmament.features.mining

import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import net.minecraft.world.level.block.Blocks
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.network.chat.Component
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.ChestInventoryUpdateEvent
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TemplateUtil
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.accessors.castAccessor
import moe.nea.firmament.util.customgui.CustomGui
import moe.nea.firmament.util.customgui.customGui
import moe.nea.firmament.util.mc.CommonTextures
import moe.nea.firmament.util.mc.SlotUtils.clickRightMouseButton
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object HotmPresets {
	val SHARE_PREFIX = "FIRMHOTM/"

	@Serializable
	data class HotmPreset(
		val perks: List<PerkPreset>,
	)

	@Serializable
	data class PerkPreset(val perkName: String)

	var hotmCommandSent = TimeMark.farPast()
	val hotmInventoryName = "Heart of the Mountain"

	@Subscribe
	fun onScreenOpen(event: ScreenChangeEvent) {
		val title = event.new?.title?.unformattedString
		if (title != hotmInventoryName) return
		val screen = event.new as? AbstractContainerScreen<*> ?: return
		val oldHandler = (event.old as? AbstractContainerScreen<*>)?.customGui
		if (oldHandler is HotmScrollPrompt) {
			event.new.customGui = oldHandler
			oldHandler.setNewScreen(screen)
			return
		}
		if (hotmCommandSent.passedTime() > 5.seconds) return
		hotmCommandSent = TimeMark.farPast()
		screen.customGui = HotmScrollPrompt(screen)
	}

	class HotmScrollPrompt(var screen: AbstractContainerScreen<*>) : CustomGui() {
		var bounds = Rectangle(
			0, 0, 0, 0
		)

		fun setNewScreen(screen: AbstractContainerScreen<*>) {
			this.screen = screen
			onInit()
			hasScrolled = false
		}

		override fun render(drawContext: GuiGraphicsExtractor, delta: Float, mouseX: Int, mouseY: Int) {
			drawContext.drawGuiTexture(
				CommonTextures.genericWidget(),
				bounds.x, bounds.y,
				bounds.width,
				bounds.height,
			)
			drawContext.centeredText(
				MC.font,
				if (hasAll) {
					Component.translatable("firmament.hotmpreset.copied")
				} else if (!hasScrolled) {
					Component.translatable("firmament.hotmpreset.scrollprompt")
				} else {
					Component.translatable("firmament.hotmpreset.scrolled")
				},
				bounds.centerX,
				bounds.centerY - 5,
				-1
			)
		}


		var hasScrolled = false
		var hasAll = false

		override fun mouseClick(click: MouseButtonEvent, doubled: Boolean): Boolean {
			if (!hasScrolled) {
				val slot = screen.menu.getSlot(8)
				println("Clicking ${slot.item}")
				slot.clickRightMouseButton(screen.menu)
			}
			hasScrolled = true
			return super.mouseClick(click, doubled)
		}

		override fun shouldDrawForeground(): Boolean {
			return false
		}

		override fun getBounds(): List<Rectangle> {
			return listOf(bounds)
		}

		override fun onInit() {
			bounds = Rectangle(
				screen.width / 2 - 150,
				screen.height / 2 - 100,
				300, 200
			)
			val screen = screen.castAccessor()
			screen.x_Firmament = bounds.x
			screen.y_Firmament = bounds.y
			screen.backgroundWidth_Firmament = bounds.width
			screen.backgroundHeight_Firmament = bounds.height
		}

		override fun moveSlot(slot: Slot) {
			slot.x = -10000
		}

		val coveredRows = mutableSetOf<Int>()
		val unlockedPerks = mutableSetOf<String>()
		val allRows = (1..10).toSet()

		fun onNewItems(event: ChestInventoryUpdateEvent) {
			val handler = screen.menu as? ChestMenu ?: return
			for (it in handler.slots) {
				if (it.container is Inventory) continue
				val stack = it.item
				val name = stack.accessor().displayNameAccordingToNbt.unformattedString
				tierRegex.useMatch(name) {
					coveredRows.add(group("tier").toInt())
				}
				if (stack.item == Items.DIAMOND
					|| stack.item == Items.EMERALD
					|| stack.item == Blocks.EMERALD_BLOCK.asItem()
				) {
					unlockedPerks.add(name)
				}
			}
			if (allRows == coveredRows) {
				ClipboardUtils.setTextContent(
					TemplateUtil.encodeTemplate(
					SHARE_PREFIX, HotmPreset(
					unlockedPerks.map { PerkPreset(it) }
				)))
				hasAll = true
			}
		}
	}

	val tierRegex = "Tier (?<tier>[0-9]+)".toPattern()
	var highlightedPerks: Set<String> = emptySet()

	@Subscribe
	fun onSlotUpdates(event: ChestInventoryUpdateEvent) {
		val customGui = (event.inventory as? AbstractContainerScreen<*>)?.customGui
		if (customGui is HotmScrollPrompt) {
			customGui.onNewItems(event)
		}
	}

	@Subscribe
	fun resetOnScreen(event: ScreenChangeEvent) {
		if (event.new != null && event.new.title.unformattedString != hotmInventoryName) {
			highlightedPerks = emptySet()
		}
	}

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (hotmInventoryName == MC.screenName
			&& event.slot.item.accessor().displayNameAccordingToNbt.unformattedString in highlightedPerks
		) {
			event.highlight((Firmament.identifier("hotm_perk_preset")))
		}
	}

	@Subscribe
	fun onCommand(event: CommandEvent.SubCommand) {
		event.subcommand("exporthotm") {
			thenExecute {
				hotmCommandSent = TimeMark.now()
				MC.sendCommand("hotm")
				source.sendFeedback(Component.translatable("firmament.hotmpreset.openinghotm"))
			}
		}
		event.subcommand("importhotm") {
			thenExecute {
				val template =
					TemplateUtil.maybeDecodeTemplate<HotmPreset>(SHARE_PREFIX, ClipboardUtils.getTextContents())
				if (template == null) {
					source.sendFeedback(Component.translatable("firmament.hotmpreset.failedimport"))
				} else {
					highlightedPerks = template.perks.mapTo(mutableSetOf()) { it.perkName }
					source.sendFeedback(Component.translatable("firmament.hotmpreset.okayimport"))
					MC.sendCommand("hotm")
				}
			}
		}
	}

}

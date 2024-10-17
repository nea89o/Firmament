package moe.nea.firmament.features.mining

import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.ChestInventoryUpdateEvent
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TemplateUtil
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.customgui.CustomGui
import moe.nea.firmament.util.customgui.customGui
import moe.nea.firmament.util.mc.CommonTextures
import moe.nea.firmament.util.mc.SlotUtils.clickRightMouseButton
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object HotmPresets {
	object Config : ManagedConfig("hotm-presets", Category.MINING) {
	}

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
		val screen = event.new as? HandledScreen<*> ?: return
		val oldHandler = (event.old as? HandledScreen<*>)?.customGui
		if (oldHandler is HotmScrollPrompt) {
			event.new.customGui = oldHandler
			oldHandler.setNewScreen(screen)
			return
		}
		if (hotmCommandSent.passedTime() > 5.seconds) return
		hotmCommandSent = TimeMark.farPast()
		screen.customGui = HotmScrollPrompt(screen)
	}

	class HotmScrollPrompt(var screen: HandledScreen<*>) : CustomGui() {
		var bounds = Rectangle(
			0, 0, 0, 0
		)

		fun setNewScreen(screen: HandledScreen<*>) {
			this.screen = screen
			onInit()
			hasScrolled = false
		}

		override fun render(drawContext: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
			drawContext.drawGuiTexture(
				CommonTextures.genericWidget(),
				bounds.x, bounds.y, 0,
				bounds.width,
				bounds.height,
			)
			drawContext.drawCenteredTextWithShadow(
				MC.font,
				if (hasAll) {
					Text.translatable("firmament.hotmpreset.copied")
				} else if (!hasScrolled) {
					Text.translatable("firmament.hotmpreset.scrollprompt")
				} else {
					Text.translatable("firmament.hotmpreset.scrolled")
				},
				bounds.centerX,
				bounds.centerY - 5,
				-1
			)
		}


		var hasScrolled = false
		var hasAll = false

		override fun mouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (!hasScrolled) {
				val slot = screen.screenHandler.getSlot(8)
				println("Clicking ${slot.stack}")
				slot.clickRightMouseButton(screen.screenHandler)
			}
			hasScrolled = true
			return super.mouseClick(mouseX, mouseY, button)
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
			val screen = screen as AccessorHandledScreen
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
			val handler = screen.screenHandler as? GenericContainerScreenHandler ?: return
			for (it in handler.slots) {
				if (it.inventory is PlayerInventory) continue
				val stack = it.stack
				val name = stack.displayNameAccordingToNbt.unformattedString
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
				ClipboardUtils.setTextContent(TemplateUtil.encodeTemplate(SHARE_PREFIX, HotmPreset(
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
		val customGui = (event.inventory as? HandledScreen<*>)?.customGui
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
			&& event.slot.stack.displayNameAccordingToNbt.unformattedString in highlightedPerks
		) {
			event.highlight(MC.guiAtlasManager.getSprite(Firmament.identifier("hotm_perk_preset")))
		}
	}

	@Subscribe
	fun onCommand(event: CommandEvent.SubCommand) {
		event.subcommand("exporthotm") {
			thenExecute {
				hotmCommandSent = TimeMark.now()
				MC.sendCommand("hotm")
				source.sendFeedback(Text.translatable("firmament.hotmpreset.openinghotm"))
			}
		}
		event.subcommand("importhotm") {
			thenExecute {
				val template =
					TemplateUtil.maybeDecodeTemplate<HotmPreset>(SHARE_PREFIX, ClipboardUtils.getTextContents())
				if (template == null) {
					source.sendFeedback(Text.translatable("firmament.hotmpreset.failedimport"))
				} else {
					highlightedPerks = template.perks.mapTo(mutableSetOf()) { it.perkName }
					source.sendFeedback(Text.translatable("firmament.hotmpreset.okayimport"))
					MC.sendCommand("hotm")
				}
			}
		}
	}

}

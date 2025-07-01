package moe.nea.firmament.features.inventory.buttons

import io.github.notenoughupdates.moulconfig.common.IItemStack
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.platform.ModernItemStack
import io.github.notenoughupdates.moulconfig.platform.ModernRenderContext
import io.github.notenoughupdates.moulconfig.xml.Bind
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import org.lwjgl.glfw.GLFW
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.FragmentGuiScreen
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.tr

class InventoryButtonEditor(
	val lastGuiRect: Rectangle,
) : FragmentGuiScreen() {
	inner class Editor(val originalButton: InventoryButton) {
		@field:Bind
		var command: String = originalButton.command ?: ""

		@field:Bind
		var icon: String = originalButton.icon ?: ""

		@Bind
		fun getItemIcon(): IItemStack {
			save()
			return ModernItemStack.of(InventoryButton.getItemForName(icon))
		}

		@Bind
		fun delete() {
			buttons.removeIf { it === originalButton }
			popup = null
		}

		fun save() {
			originalButton.icon = icon
			originalButton.command = command
		}
	}

	var buttons: MutableList<InventoryButton> =
		InventoryButtons.DConfig.data.buttons.map { it.copy() }.toMutableList()

	override fun close() {
		InventoryButtons.DConfig.data.buttons = buttons
		InventoryButtons.DConfig.markDirty()
		super.close()
	}

	override fun resize(client: MinecraftClient, width: Int, height: Int) {
		lastGuiRect.move(
			MC.window.scaledWidth / 2 - lastGuiRect.width / 2,
			MC.window.scaledHeight / 2 - lastGuiRect.height / 2
		)
		super.resize(client, width, height)
	}

	override fun init() {
		super.init()
		addDrawableChild(
			TextWidget(
				lastGuiRect.minX,
				25,
				lastGuiRect.width,
				9,
				Text.translatable("firmament.inventory-buttons.delete"),
				MC.font
			).alignCenter()
		)
		addDrawableChild(
			TextWidget(
				lastGuiRect.minX,
				40,
				lastGuiRect.width,
				9,
				Text.translatable("firmament.inventory-buttons.info"),
				MC.font
			).alignCenter()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.reset")) {
				val newButtons = InventoryButtonTemplates.loadTemplate("TkVVQlVUVE9OUy9bXQ==")
				if (newButtons != null)
					buttons = moveButtons(newButtons.map { it.copy(command = it.command?.removePrefix("/")) })
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 10)
				.width(lastGuiRect.width - 20)
				.build()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.load-preset")) {
				val t = ClipboardUtils.getTextContents()
				val newButtons = InventoryButtonTemplates.loadTemplate(t)
				if (newButtons != null)
					buttons = moveButtons(newButtons.map { it.copy(command = it.command?.removePrefix("/")) })
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 35)
				.width(lastGuiRect.width - 20)
				.build()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.save-preset")) {
				ClipboardUtils.setTextContent(InventoryButtonTemplates.saveTemplate(buttons))
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 60)
				.width(lastGuiRect.width - 20)
				.build()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.simple-preset")) {
				// Preset from NEU
				// Credit: https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/9b1fcfebc646e9fb69f99006327faa3e734e5f51/src/main/resources/assets/notenoughupdates/invbuttons/presets.json#L900-L1348
				val newButtons = InventoryButtonTemplates.loadTemplate("TkVVQlVUVE9OUy9bIntcblx0XCJ4XCI6IDE2MCxcblx0XCJ5XCI6IC0yMCxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcImJvbmVcIixcblx0XCJjb21tYW5kXCI6IFwicGV0c1wiXG59Iiwie1xuXHRcInhcIjogMTQwLFxuXHRcInlcIjogLTIwLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiBmYWxzZSxcblx0XCJpY29uXCI6IFwiYXJtb3Jfc3RhbmRcIixcblx0XCJjb21tYW5kXCI6IFwid2FyZHJvYmVcIlxufSIsIntcblx0XCJ4XCI6IDEyMCxcblx0XCJ5XCI6IC0yMCxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcImVuZGVyX2NoZXN0XCIsXG5cdFwiY29tbWFuZFwiOiBcInN0b3JhZ2VcIlxufSIsIntcblx0XCJ4XCI6IDEwMCxcblx0XCJ5XCI6IC0yMCxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcInNrdWxsOmQ3Y2M2Njg3NDIzZDA1NzBkNTU2YWM1M2UwNjc2Y2I1NjNiYmRkOTcxN2NkODI2OWJkZWJlZDZmNmQ0ZTdiZjhcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBpc2xhbmRcIlxufSIsIntcblx0XCJ4XCI6IDgwLFxuXHRcInlcIjogLTIwLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiBmYWxzZSxcblx0XCJpY29uXCI6IFwic2t1bGw6MzVmNGI0MGNlZjllMDE3Y2Q0MTEyZDI2YjYyNTU3ZjhjMWQ1YjE4OWRhMmU5OTUzNDIyMmJjOGNlYzdkOTE5NlwiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGh1YlwiXG59Il0=")
				if (newButtons != null)
					buttons = moveButtons(newButtons.map { it.copy(command = it.command?.removePrefix("/")) })
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 85)
				.width(lastGuiRect.width - 20)
				.build()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.all-warps-preset")) {
				// Preset from NEU
				// Credit: https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/9b1fcfebc646e9fb69f99006327faa3e734e5f51/src/main/resources/assets/notenoughupdates/invbuttons/presets.json#L1817-L2276
				val newButtons = InventoryButtonTemplates.loadTemplate("TkVVQlVUVE9OUy9bIntcblx0XCJ4XCI6IDIsXG5cdFwieVwiOiAtODQsXG5cdFwiYW5jaG9yUmlnaHRcIjogdHJ1ZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6YzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NlwiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGhvbWVcIlxufSIsIntcblx0XCJ4XCI6IDIsXG5cdFwieVwiOiAtNjQsXG5cdFwiYW5jaG9yUmlnaHRcIjogdHJ1ZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6ZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOFwiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGh1YlwiXG59Iiwie1xuXHRcInhcIjogMixcblx0XCJ5XCI6IC00NCxcblx0XCJhbmNob3JSaWdodFwiOiB0cnVlLFxuXHRcImFuY2hvckJvdHRvbVwiOiB0cnVlLFxuXHRcImljb25cIjogXCJza3VsbDo5YjU2ODk1Yjk2NTk4OTZhZDY0N2Y1ODU5OTIzOGFmNTMyZDQ2ZGI5YzFiMDM4OWI4YmJlYjcwOTk5ZGFiMzNkXCIsXG5cdFwiY29tbWFuZFwiOiBcIndhcnAgZHVuZ2Vvbl9odWJcIlxufSIsIntcblx0XCJ4XCI6IDIsXG5cdFwieVwiOiAtMjQsXG5cdFwiYW5jaG9yUmlnaHRcIjogdHJ1ZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6Nzg0MGI4N2Q1MjI3MWQyYTc1NWRlZGM4Mjg3N2UwZWQzZGY2N2RjYzQyZWE0NzllYzE0NjE3NmIwMjc3OWE1XCIsXG5cdFwiY29tbWFuZFwiOiBcIndhcnAgZW5kXCJcbn0iLCJ7XG5cdFwieFwiOiAxMDksXG5cdFwieVwiOiAtMTksXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IGZhbHNlLFxuXHRcImljb25cIjogXCJza3VsbDo4NmYwNmVhYTMwMDRhZWVkMDliM2Q1YjQ1ZDk3NmRlNTg0ZTY5MWMwZTljYWRlMTMzNjM1ZGU5M2QyM2I5ZWRiXCIsXG5cdFwiY29tbWFuZFwiOiBcImhvdG1cIlxufSIsIntcblx0XCJ4XCI6IDEzMCxcblx0XCJ5XCI6IC0xOSxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcIkVOREVSX0NIRVNUXCIsXG5cdFwiY29tbWFuZFwiOiBcInN0b3JhZ2VcIlxufSIsIntcblx0XCJ4XCI6IDE1MSxcblx0XCJ5XCI6IC0xOSxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcIkJPTkVcIixcblx0XCJjb21tYW5kXCI6IFwicGV0c1wiXG59Iiwie1xuXHRcInhcIjogLTE5LFxuXHRcInlcIjogMixcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogZmFsc2UsXG5cdFwiaWNvblwiOiBcIkdPTERfQkxPQ0tcIixcblx0XCJjb21tYW5kXCI6IFwiYWhcIlxufSIsIntcblx0XCJ4XCI6IC0xOSxcblx0XCJ5XCI6IDIyLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiBmYWxzZSxcblx0XCJpY29uXCI6IFwiR09MRF9CQVJESU5HXCIsXG5cdFwiY29tbWFuZFwiOiBcImJ6XCJcbn0iLCJ7XG5cdFwieFwiOiAtMTksXG5cdFwieVwiOiAtODQsXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IHRydWUsXG5cdFwiaWNvblwiOiBcInNrdWxsOjQzOGNmM2Y4ZTU0YWZjM2IzZjkxZDIwYTQ5ZjMyNGRjYTE0ODYwMDdmZTU0NTM5OTA1NTUyNGMxNzk0MWY0ZGNcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBtdXNldW1cIlxufSIsIntcblx0XCJ4XCI6IC0xOSxcblx0XCJ5XCI6IC02NCxcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6ZjQ4ODBkMmMxZTdiODZlODc1MjJlMjA4ODI2NTZmNDViYWZkNDJmOTQ5MzJiMmM1ZTBkNmVjYWE0OTBjYjRjXCIsXG5cdFwiY29tbWFuZFwiOiBcIndhcnAgZ2FyZGVuXCJcbn0iLCJ7XG5cdFwieFwiOiAtMTksXG5cdFwieVwiOiAtNDQsXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IHRydWUsXG5cdFwiaWNvblwiOiBcInNrdWxsOjRkM2E2YmQ5OGFjMTgzM2M2NjRjNDkwOWZmOGQyZGM2MmNlODg3YmRjZjNjYzViMzg0ODY1MWFlNWFmNmJcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBiYXJuXCJcbn0iLCJ7XG5cdFwieFwiOiAtMTksXG5cdFwieVwiOiAtMjQsXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IHRydWUsXG5cdFwiaWNvblwiOiBcInNrdWxsOjUxNTM5ZGRkZjllZDI1NWVjZTYzNDgxOTNjZDc1MDEyYzgyYzkzYWVjMzgxZjA1NTcyY2VjZjczNzk3MTFiM2JcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBkZXNlcnRcIlxufSIsIntcblx0XCJ4XCI6IDQsXG5cdFwieVwiOiAyLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiB0cnVlLFxuXHRcImljb25cIjogXCJza3VsbDo3M2JjOTY1ZDU3OWMzYzYwMzlmMGExN2ViN2MyZTZmYWY1MzhjN2E1ZGU4ZTYwZWM3YTcxOTM2MGQwYTg1N2E5XCIsXG5cdFwiY29tbWFuZFwiOiBcIndhcnAgZ29sZFwiXG59Iiwie1xuXHRcInhcIjogMjUsXG5cdFwieVwiOiAyLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiB0cnVlLFxuXHRcImljb25cIjogXCJza3VsbDo1NjlhMWYxMTQxNTFiNDUyMTM3M2YzNGJjMTRjMjk2M2E1MDExY2RjMjVhNjU1NGM0OGM3MDhjZDk2ZWJmY1wiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGRlZXBcIlxufSIsIntcblx0XCJ4XCI6IDQ2LFxuXHRcInlcIjogMixcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6MjFkYmUzMGIwMjdhY2JjZWI2MTI1NjNiZDg3N2NkN2ViYjcxOWVhNmVkMTM5OTAyN2RjZWU1OGJiOTA0OWQ0YVwiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGNyeXN0YWxzXCJcbn0iLCJ7XG5cdFwieFwiOiA2Nyxcblx0XCJ5XCI6IDIsXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IHRydWUsXG5cdFwiaWNvblwiOiBcInNrdWxsOjVjYmQ5ZjVlYzFlZDAwNzI1OTk5NjQ5MWU2OWZmNjQ5YTMxMDZjZjkyMDIyN2IxYmIzYTcxZWU3YTg5ODYzZlwiLFxuXHRcImNvbW1hbmRcIjogXCJ3YXJwIGZvcmdlXCJcbn0iLCJ7XG5cdFwieFwiOiA4OCxcblx0XCJ5XCI6IDIsXG5cdFwiYW5jaG9yUmlnaHRcIjogZmFsc2UsXG5cdFwiYW5jaG9yQm90dG9tXCI6IHRydWUsXG5cdFwiaWNvblwiOiBcInNrdWxsOjZiMjBiMjNjMWFhMmJlMDI3MGYwMTZiNGM5MGQ2ZWU2YjgzMzBhMTdjZmVmODc4NjlkNmFkNjBiMmZmYmYzYjVcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBtaW5lc1wiXG59Iiwie1xuXHRcInhcIjogMTA5LFxuXHRcInlcIjogMixcblx0XCJhbmNob3JSaWdodFwiOiBmYWxzZSxcblx0XCJhbmNob3JCb3R0b21cIjogdHJ1ZSxcblx0XCJpY29uXCI6IFwic2t1bGw6YTIyMWY4MTNkYWNlZTBmZWY4YzU5Zjc2ODk0ZGJiMjY0MTU0NzhkOWRkZmM0NGMyZTcwOGE2ZDNiNzU0OWJcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBwYXJrXCJcbn0iLCJ7XG5cdFwieFwiOiAxMzAsXG5cdFwieVwiOiAyLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiB0cnVlLFxuXHRcImljb25cIjogXCJza3VsbDo5ZDdlM2IxOWFjNGYzZGVlOWM1Njc3YzEzNTMzM2I5ZDM1YTdmNTY4YjYzZDFlZjRhZGE0YjA2OGI1YTI1XCIsXG5cdFwiY29tbWFuZFwiOiBcIndhcnAgc3BpZGVyXCJcbn0iLCJ7XG5cdFwieFwiOiAxNTEsXG5cdFwieVwiOiAyLFxuXHRcImFuY2hvclJpZ2h0XCI6IGZhbHNlLFxuXHRcImFuY2hvckJvdHRvbVwiOiB0cnVlLFxuXHRcImljb25cIjogXCJza3VsbDpjMzY4N2UyNWM2MzJiY2U4YWE2MWUwZDY0YzI0ZTY5NGMzZWVhNjI5ZWE5NDRmNGNmMzBkY2ZiNGZiY2UwNzFcIixcblx0XCJjb21tYW5kXCI6IFwid2FycCBuZXRoZXJcIlxufSJd")
				if (newButtons != null)
					buttons = moveButtons(newButtons.map { it.copy(command = it.command?.removePrefix("/")) })
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 110)
				.width(lastGuiRect.width - 20)
				.build()
		)
	}

	private fun moveButtons(buttons: List<InventoryButton>): MutableList<InventoryButton> {
		val newButtons: MutableList<InventoryButton> = ArrayList(buttons.size)
		val movedButtons = mutableListOf<InventoryButton>()
		for (button in buttons) {
			if ((!button.anchorBottom && !button.anchorRight && button.x > 0 && button.y > 0)) {
				MC.sendChat(
					tr(
						"firmament.inventory-buttons.button-moved",
						"One of your imported buttons intersects with the inventory and has been moved to the top left."
					)
				)
				movedButtons.add(
					button.copy(
						x = 0,
						y = -InventoryButton.dimensions.width,
						anchorRight = false,
						anchorBottom = false
					)
				)
			} else {
				newButtons.add(button)
			}
		}
		var i = 0
		val zeroRect = Rectangle(0, 0, 1, 1)
		for (movedButton in movedButtons) {
			fun getPosition(button: InventoryButton, index: Int) =
				button.copy(
					x = (index % 10) * InventoryButton.dimensions.width,
					y = (index / 10) * -InventoryButton.dimensions.height,
					anchorRight = false, anchorBottom = false
				)
			while (true) {
				val newPos = getPosition(movedButton, i++)
				val newBounds = newPos.getBounds(zeroRect)
				if (newButtons.none { it.getBounds(zeroRect).intersects(newBounds) }) {
					newButtons.add(newPos)
					break
				}
			}
		}
		return newButtons
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		context.matrices.push()
		context.matrices.translate(0F, 0F, -15F)
		super.renderBackground(context, mouseX, mouseY, delta)
		context.matrices.pop()
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		context.matrices.push()
		context.matrices.translate(0f, 0f, -10f)
		PanelComponent.DefaultBackgroundRenderer.VANILLA
			.render(
				ModernRenderContext(context),
				lastGuiRect.minX, lastGuiRect.minY,
				lastGuiRect.width, lastGuiRect.height,
			)
		context.matrices.pop()
		for (button in buttons) {
			val buttonPosition = button.getBounds(lastGuiRect)
			context.matrices.push()
			context.matrices.translate(buttonPosition.minX.toFloat(), buttonPosition.minY.toFloat(), 0F)
			button.render(context)
			context.matrices.pop()
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close()
			return true
		}
		return false
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseReleased(mouseX, mouseY, button)) return true
		val clickedButton = buttons.firstOrNull { it.getBounds(lastGuiRect).contains(Point(mouseX, mouseY)) }
		if (clickedButton != null && !justPerformedAClickAction) {
			if (InputUtil.isKeyPressed(MC.window.handle, InputUtil.GLFW_KEY_LEFT_CONTROL)) Editor(clickedButton).delete()
			else createPopup(MoulConfigUtils.loadGui("button_editor_fragment", Editor(clickedButton)), Point(mouseX, mouseY))
			return true
		}
		justPerformedAClickAction = false
		lastDraggedButton = null
		return false
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true

		if (initialDragMousePosition.distanceSquared(Vec2f(mouseX.toFloat(), mouseY.toFloat())) >= 4 * 4) {
			initialDragMousePosition = Vec2f(-10F, -10F)
			lastDraggedButton?.let { dragging ->
				justPerformedAClickAction = true
				val (anchorRight, anchorBottom, offsetX, offsetY) = getCoordsForMouse(mouseX.toInt(), mouseY.toInt())
					?: return true
				dragging.x = offsetX
				dragging.y = offsetY
				dragging.anchorRight = anchorRight
				dragging.anchorBottom = anchorBottom
			}
		}
		return false
	}

	var lastDraggedButton: InventoryButton? = null
	var justPerformedAClickAction = false
	var initialDragMousePosition = Vec2f(-10F, -10F)

	data class AnchoredCoords(
		val anchorRight: Boolean,
		val anchorBottom: Boolean,
		val offsetX: Int,
		val offsetY: Int,
	)

	fun getCoordsForMouse(mx: Int, my: Int): AnchoredCoords? {
		val anchorRight = mx > lastGuiRect.maxX
		val anchorBottom = my > lastGuiRect.maxY
		var offsetX = mx - if (anchorRight) lastGuiRect.maxX else lastGuiRect.minX
		var offsetY = my - if (anchorBottom) lastGuiRect.maxY else lastGuiRect.minY
		if (InputUtil.isKeyPressed(MC.window.handle, InputUtil.GLFW_KEY_LEFT_SHIFT)) {
			offsetX = MathHelper.floor(offsetX / 20F) * 20
			offsetY = MathHelper.floor(offsetY / 20F) * 20
		}
		val rect = InventoryButton(offsetX, offsetY, anchorRight, anchorBottom).getBounds(lastGuiRect)
		if (rect.intersects(lastGuiRect)) return null
		val anchoredCoords = AnchoredCoords(anchorRight, anchorBottom, offsetX, offsetY)
		return anchoredCoords
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseClicked(mouseX, mouseY, button)) return true
		val clickedButton = buttons.firstOrNull { it.getBounds(lastGuiRect).contains(Point(mouseX, mouseY)) }
		if (clickedButton != null) {
			lastDraggedButton = clickedButton
			initialDragMousePosition = Vec2f(mouseX.toFloat(), mouseY.toFloat())
			return true
		}
		val mx = mouseX.toInt()
		val my = mouseY.toInt()
		val (anchorRight, anchorBottom, offsetX, offsetY) = getCoordsForMouse(mx, my) ?: return true
		buttons.add(InventoryButton(offsetX, offsetY, anchorRight, anchorBottom, null, null))
		justPerformedAClickAction = true
		return true
	}

}



package moe.nea.firmament.features.inventory.buttons

import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenClickEvent
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.HandledScreenPushREIEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.data.DataHolder
import moe.nea.firmament.util.accessors.getRectangle
import moe.nea.firmament.util.gold

object InventoryButtons {

    object TConfig : ManagedConfig("inventory-buttons-config", Category.INVENTORY) {
        val _openEditor by button("open-editor") {
            openEditor()
        }
		val hoverText by toggle("hover-text") { true }
		val onlyInv by toggle("only-inv") { false }
    }

    object DConfig : DataHolder<Data>(serializer(), "inventory-buttons", ::Data)

    @Serializable
    data class Data(
        var buttons: MutableList<InventoryButton> = mutableListOf()
    )

	fun getValidButtons(screen: HandledScreen<*>): Sequence<InventoryButton> {
		return DConfig.data.buttons.asSequence().filter { button ->
			button.isValid() && (!TConfig.onlyInv || screen is InventoryScreen)
		}
	}


    @Subscribe
    fun onRectangles(it: HandledScreenPushREIEvent) {
        val bounds = it.screen.getRectangle()
        for (button in getValidButtons(it.screen)) {
            val buttonBounds = button.getBounds(bounds)
            it.block(buttonBounds)
        }
    }

    @Subscribe
    fun onClickScreen(it: HandledScreenClickEvent) {
        val bounds = it.screen.getRectangle()
        for (button in getValidButtons(it.screen)) {
            val buttonBounds = button.getBounds(bounds)
            if (buttonBounds.contains(it.mouseX, it.mouseY)) {
                MC.sendCommand(button.command!! /* non null invariant covered by getValidButtons */)
                break
            }
        }
    }

	var lastHoveredComponent: InventoryButton? = null
	var lastMouseMove = TimeMark.farPast()

    @Subscribe
    fun onRenderForeground(it: HandledScreenForegroundEvent) {
		val bounds = it.screen.getRectangle()

		var hoveredComponent: InventoryButton? = null
		for (button in getValidButtons(it.screen)) {
            val buttonBounds = button.getBounds(bounds)
            it.context.matrices.push()
            it.context.matrices.translate(buttonBounds.minX.toFloat(), buttonBounds.minY.toFloat(), 0F)
            button.render(it.context)
            it.context.matrices.pop()

			if (buttonBounds.contains(it.mouseX, it.mouseY) && TConfig.hoverText && hoveredComponent == null) {
				hoveredComponent = button
				if (lastMouseMove.passedTime() > 0.6.seconds && lastHoveredComponent === button) {
					it.context.drawTooltip(
						MC.font,
						listOf(Text.literal(button.command).gold()),
						buttonBounds.minX - 15,
						buttonBounds.maxY + 20,
					)
				}
			}
        }
		if (hoveredComponent !== lastHoveredComponent)
			lastMouseMove = TimeMark.now()
		lastHoveredComponent = hoveredComponent
        lastRectangle = bounds
    }

    var lastRectangle: Rectangle? = null
    fun openEditor() {
        ScreenUtil.setScreenLater(
            InventoryButtonEditor(
                lastRectangle ?: Rectangle(
                    MC.window.scaledWidth / 2 - 88,
                    MC.window.scaledHeight / 2 - 83,
                    176, 166,
                )
            )
        )
    }
}

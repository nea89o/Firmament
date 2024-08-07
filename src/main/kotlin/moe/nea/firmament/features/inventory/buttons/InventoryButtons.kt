

package moe.nea.firmament.features.inventory.buttons

import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenClickEvent
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.HandledScreenPushREIEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.data.DataHolder
import moe.nea.firmament.util.getRectangle

object InventoryButtons : FirmamentFeature {
    override val identifier: String
        get() = "inventory-buttons"

    object TConfig : ManagedConfig(identifier) {
        val _openEditor by button("open-editor") {
            openEditor()
        }
    }

    object DConfig : DataHolder<Data>(serializer(), identifier, ::Data)

    @Serializable
    data class Data(
        var buttons: MutableList<InventoryButton> = mutableListOf()
    )


    override val config: ManagedConfig
        get() = TConfig

    fun getValidButtons() = DConfig.data.buttons.asSequence().filter { it.isValid() }

    @Subscribe
    fun onRectangles(it: HandledScreenPushREIEvent) {
        val bounds = it.screen.getRectangle()
        for (button in getValidButtons()) {
            val buttonBounds = button.getBounds(bounds)
            it.block(buttonBounds)
        }
    }

    @Subscribe
    fun onClickScreen(it: HandledScreenClickEvent) {
        val bounds = it.screen.getRectangle()
        for (button in getValidButtons()) {
            val buttonBounds = button.getBounds(bounds)
            if (buttonBounds.contains(it.mouseX, it.mouseY)) {
                MC.sendCommand(button.command!! /* non null invariant covered by getValidButtons */)
                break
            }
        }
    }

    @Subscribe
    fun onRenderForeground(it: HandledScreenForegroundEvent) {
        val bounds = it.screen.getRectangle()
        for (button in getValidButtons()) {
            val buttonBounds = button.getBounds(bounds)
            it.context.matrices.push()
            it.context.matrices.translate(buttonBounds.minX.toFloat(), buttonBounds.minY.toFloat(), 0F)
            button.render(it.context)
            it.context.matrices.pop()
        }
        lastRectangle = bounds
    }

    var lastRectangle: Rectangle? = null
    fun openEditor() {
        ScreenUtil.setScreenLater(
            InventoryButtonEditor(
                lastRectangle ?: Rectangle(
                    MC.window.scaledWidth / 2 - 100,
                    MC.window.scaledHeight / 2 - 100,
                    200, 200,
                )
            )
        )
    }
}

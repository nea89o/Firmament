

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.gui.FixedComponent

class GuiAppender(val width: Int, val screenAccessor: () -> Screen) {
    val panel = mutableListOf<GuiComponent>()
    internal val reloadables = mutableListOf<(() -> Unit)>()

    fun onReload(reloadable: () -> Unit) {
        reloadables.add(reloadable)
    }

    fun appendLabeledRow(label: Text, right: GuiComponent) {
        appendSplitRow(
            TextComponent(label.string),
            right
        )
    }

    fun appendSplitRow(left: GuiComponent, right: GuiComponent) {
        // TODO: make this more dynamic
        // i could just make a component that allows for using half the available size
        appendFullRow(RowComponent(
            FixedComponent(GetSetter.constant(width / 2), null, left),
            FixedComponent(GetSetter.constant(width / 2), null, right),
        ))
    }

    fun appendFullRow(widget: GuiComponent) {
        panel.add(widget)
    }
}

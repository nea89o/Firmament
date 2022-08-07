package moe.nea.notenoughupdates.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.util.ConfigHolder
import net.minecraft.network.chat.Component
import kotlin.reflect.KMutableProperty1

class ConfigGui<K>(val holder: ConfigHolder<K>, val build: ConfigGui<K>.() -> Unit) : LightweightGuiDescription() {
    private val root = WGridPanelWithPadding(verticalPadding = 4)
    private val reloadables = mutableListOf<(() -> Unit)>()

    init {
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL
        build()
        reload()
    }

    fun title(component: Component) {
        if (col != 0) {
            NotEnoughUpdates.logger.warn("Set title not at the top of the ConfigGui")
        }
        val label = WLabel(component)
        label.verticalAlignment = VerticalAlignment.TOP
        label.horizontalAlignment = HorizontalAlignment.CENTER
        root.add(label, 0, col, 11, 1)
        col++
    }

    private fun label(component: Component) {
        val label = WLabel(component)
        label.verticalAlignment = VerticalAlignment.CENTER
        root.add(label, 0, col, 5, 1)
    }

    fun toggle(component: Component, prop: KMutableProperty1<K, Boolean>) {
        val toggle = WToggleButton(component)
        reloadables.add { toggle.toggle = prop.get(holder.config) }
        toggle.setOnToggle {
            prop.set(holder.config, true)
            holder.markDirty()
        }
        root.add(toggle, 5, col, 6, 1)
        label(component)
        col++
    }

    fun button(component: Component, buttonText: Component, runnable: () -> Unit) {
        val button = WButton(buttonText)
        button.setOnClick {
            runnable.invoke()
        }
        root.add(button, 5, col, 6, 1)
        label(component)
        col++
    }

    fun textfield(
        component: Component,
        background: Component,
        prop: KMutableProperty1<K, String>,
        maxLength: Int = 255
    ) {
        val textfield = WTextField(background)
        textfield.isEditable = true
        reloadables.add {
            textfield.text = prop.get(holder.config)
        }
        textfield.maxLength = maxLength
        textfield.setChangedListener {
            prop.set(holder.config, it)
            holder.markDirty()
        }
        root.add(textfield, 5, col, 6, 11)
        label(component)
        col++
    }

    fun reload() {
        reloadables.forEach { it.invoke() }
    }

    private var col = 0


}

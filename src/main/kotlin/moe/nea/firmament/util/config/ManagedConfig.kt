package moe.nea.firmament.util.config

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.WGridPanelWithPadding
import moe.nea.firmament.util.ScreenUtil.setScreenLater

abstract class ManagedConfig(val name: String) {

    class GuiAppender(val width: Int) {
        private var row = 0
        internal val panel = WGridPanelWithPadding(verticalPadding = 4, horizontalPadding = 4)
        internal val reloadables = mutableListOf<(() -> Unit)>()
        fun set(x: Int, y: Int, w: Int, h: Int, widget: WWidget) {
            panel.add(widget, x, y, w, h)
        }


        fun onReload(reloadable: () -> Unit) {
            reloadables.add(reloadable)
        }

        fun skipRows(r: Int) {
            row += r
        }

        fun appendSplitRow(left: WWidget, right: WWidget) {
            val lw = width / 2
            set(0, row, lw, 1, left)
            set(lw, row, width - lw, 1, right)
            skipRows(1)
        }

        fun appendFullRow(widget: WWidget) {
            set(0, row, width, 1, widget)
            skipRows(1)
        }
    }

    interface OptionHandler<T : Any> {
        fun toJson(element: T): JsonElement?
        fun fromJson(element: JsonElement): T
        fun emitGuiElements(opt: Option<T>, guiAppender: GuiAppender)
    }

    inner class Option<T : Any> internal constructor(
        val propertyName: String,
        val default: () -> T,
        val handler: OptionHandler<T>
    ) : ReadOnlyProperty<Any?, T> {

        private lateinit var _value: T
        private var loaded = false
        var value: T
            get() {
                if (!loaded)
                    load()
                return _value
            }
            set(value) {
                loaded = true
                _value = value
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value
        }

        private fun load() {
            if (data.containsKey(propertyName)) {
                try {
                    value = handler.fromJson(data[propertyName]!!)
                } catch (e: Exception) {
                    Firmament.logger.error(
                        "Exception during loading of config file $name. This will reset this config.",
                        e
                    )
                }
            }
            value = default()
        }

        internal fun toJson(): JsonElement? {
            return handler.toJson(value)
        }

        fun appendToGui(guiapp: GuiAppender) {
            handler.emitGuiElements(this, guiapp)
        }
    }

    val file = Firmament.CONFIG_DIR.resolve("$name.json")
    val data: JsonObject by lazy {
        try {
            Firmament.json.decodeFromString(
                file.readText()
            )
        } catch (e: Exception) {
            Firmament.logger.info("Could not read config $name. Loading empty config.")
            JsonObject(mutableMapOf())
        }
    }

    fun save() {
        val data = JsonObject(allOptions.mapNotNull { (key, value) ->
            value.toJson()?.let {
                key to it
            }
        }.toMap())
        file.parent.createDirectories()
        file.writeText(Firmament.json.encodeToString(data))
    }


    val allOptions = mutableMapOf<String, Option<*>>()
    val sortedOptions = mutableListOf<Option<*>>()

    protected fun <T : Any> option(propertyName: String, default: () -> T, handler: OptionHandler<T>): Option<T> {
        if (propertyName in allOptions) error("Cannot register the same name twice")
        return Option(propertyName, default, handler).also {
            allOptions[propertyName] = it
            sortedOptions.add(it)
        }
    }

    class BooleanHandler(val config: ManagedConfig) : OptionHandler<Boolean> {
        override fun toJson(element: Boolean): JsonElement? {
            return JsonPrimitive(element)
        }

        override fun fromJson(element: JsonElement): Boolean {
            return element.jsonPrimitive.boolean
        }

        override fun emitGuiElements(opt: Option<Boolean>, guiAppender: GuiAppender) {
            guiAppender.appendFullRow(
                WToggleButton(Text.translatable("firmament.config.${config.name}.${opt.propertyName}")).apply {
                    guiAppender.onReload { toggle = opt.value }
                    setOnToggle {
                        opt.value = it
                        config.save()
                    }
                }
            )
        }
    }

    class ClickHandler(val config: ManagedConfig, val runnable: () -> Unit) : OptionHandler<Unit> {
        override fun toJson(element: Unit): JsonElement? {
            return null
        }

        override fun fromJson(element: JsonElement) {}

        override fun emitGuiElements(opt: Option<Unit>, guiAppender: GuiAppender) {
            guiAppender.appendSplitRow(
                WLabel(Text.translatable("firmament.config.${config.name}.${opt.propertyName}")),
                WButton(Text.translatable("firmament.config.${config.name}.${opt.propertyName}")).apply {
                    setOnClick {
                        runnable()
                    }
                },
            )
        }
    }

    protected fun toggle(propertyName: String, default: () -> Boolean): Option<Boolean> {
        return option(propertyName, default, BooleanHandler(this))
    }

    fun showConfigEditor() {
        val lwgd = LightweightGuiDescription()
        val guiapp = GuiAppender(20)
        guiapp.panel.insets = Insets.ROOT_PANEL
        sortedOptions.forEach { it.appendToGui(guiapp) }
        guiapp.reloadables.forEach { it() }
        lwgd.setRootPanel(guiapp.panel)
        setScreenLater(CottonClientScreen(lwgd))
    }

    protected fun button(propertyName: String, runnable: () -> Unit): Option<Unit> {
        return option(propertyName, { }, ClickHandler(this, runnable))
    }

}

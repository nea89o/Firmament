/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WScrollPanel
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import moe.nea.jarvis.api.Point
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.WTightScrollPanel
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil.setScreenLater

abstract class ManagedConfig(val name: String) {

    interface OptionHandler<T : Any> {
        fun toJson(element: T): JsonElement?
        fun fromJson(element: JsonElement): T
        fun emitGuiElements(opt: Option<T>, guiAppender: GuiAppender)
    }

    inner class Option<T : Any> internal constructor(
        val config: ManagedConfig,
        val propertyName: String,
        val default: () -> T,
        val handler: OptionHandler<T>
    ) : ReadWriteProperty<Any?, T> {

        val rawLabelText = "firmament.config.${config.name}.${propertyName}"
        val labelText = Text.translatable(rawLabelText)

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

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
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
            } else {
                value = default()
            }
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

    private var latestGuiAppender: GuiAppender? = null

    protected fun <T : Any> option(propertyName: String, default: () -> T, handler: OptionHandler<T>): Option<T> {
        if (propertyName in allOptions) error("Cannot register the same name twice")
        return Option(this, propertyName, default, handler).also {
            allOptions[propertyName] = it
            sortedOptions.add(it)
        }
    }

    protected fun toggle(propertyName: String, default: () -> Boolean): Option<Boolean> {
        return option(propertyName, default, BooleanHandler(this))
    }

    protected fun duration(
        propertyName: String,
        min: Duration,
        max: Duration,
        default: () -> Duration,
    ): Option<Duration> {
        return option(propertyName, default, DurationHandler(this, min, max))
    }


    protected fun position(
        propertyName: String,
        width: Int,
        height: Int,
        default: () -> Point,
    ): Option<HudMeta> {
        val label = Text.translatable("firmament.config.${name}.${propertyName}")
        return option(propertyName, {
            val p = default()
            HudMeta(HudPosition(p.x, p.y, 1F), label, width, height)
        }, HudMetaHandler(this, label, width, height))
    }

    protected fun integer(
        propertyName: String,
        min: Int,
        max: Int,
        default: () -> Int,
    ): Option<Int> {
        return option(propertyName, default, IntegerHandler(this, min, max))
    }

    protected fun button(propertyName: String, runnable: () -> Unit): Option<Unit> {
        return option(propertyName, { }, ClickHandler(this, runnable))
    }

    protected fun string(propertyName: String, default: () -> String): Option<String> {
        return option(propertyName, default, StringHandler(this))
    }



    fun reloadGui() {
        latestGuiAppender?.reloadables?.forEach {it() }
    }

    fun getConfigEditor(parent: Screen? = null): CottonClientScreen {
        val lwgd = LightweightGuiDescription()
        var screen: Screen? = null
        val guiapp = GuiAppender(400) { requireNotNull(screen) { "Screen Accessor called too early" } }
        latestGuiAppender = guiapp
        guiapp.panel.insets = Insets.ROOT_PANEL
        guiapp.appendFullRow(WBox(Axis.HORIZONTAL).also {
            it.add(WButton(Text.literal("←")).also {
                it.setOnClick {
                    AllConfigsGui.showAllGuis()
                }
            })
            it.add(WLabel(Text.translatable("firmament.config.${name}")).also {
                it.verticalAlignment = VerticalAlignment.CENTER
            })
        })
        sortedOptions.forEach { it.appendToGui(guiapp) }
        guiapp.reloadables.forEach { it() }
        lwgd.setRootPanel(WTightScrollPanel(guiapp.panel).also {
            it.setSize(400, 300)
        })
        screen =
            object : CottonClientScreen(lwgd) {
                override fun init() {
                    latestGuiAppender = guiapp
                    super.init()
                }

                override fun close() {
                    latestGuiAppender = null
                    MC.screen = parent
                }
            }
        return screen
    }

    fun showConfigEditor(parent: Screen? = null) {
        setScreenLater(getConfigEditor(parent))
    }

}

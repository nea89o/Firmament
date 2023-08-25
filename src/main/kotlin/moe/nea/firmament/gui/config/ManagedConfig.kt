/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.WTightScrollPanel
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil.setScreenLater
import moe.nea.jarvis.api.Point
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration

abstract class ManagedConfig(override val name: String) : ManagedConfigElement() {

    interface OptionHandler<T : Any> {
        fun toJson(element: T): JsonElement?
        fun fromJson(element: JsonElement): T
        fun emitGuiElements(opt: ManagedOption<T>, guiAppender: GuiAppender)
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


    val allOptions = mutableMapOf<String, ManagedOption<*>>()
    val sortedOptions = mutableListOf<ManagedOption<*>>()

    private var latestGuiAppender: GuiAppender? = null

    protected fun <T : Any> option(
        propertyName: String,
        default: () -> T,
        handler: OptionHandler<T>
    ): ManagedOption<T> {
        if (propertyName in allOptions) error("Cannot register the same name twice")
        return ManagedOption(this, propertyName, default, handler).also {
            it.load(data)
            allOptions[propertyName] = it
            sortedOptions.add(it)
        }
    }

    protected fun toggle(propertyName: String, default: () -> Boolean): ManagedOption<Boolean> {
        return option(propertyName, default, BooleanHandler(this))
    }

    protected fun duration(
        propertyName: String,
        min: Duration,
        max: Duration,
        default: () -> Duration,
    ): ManagedOption<Duration> {
        return option(propertyName, default, DurationHandler(this, min, max))
    }


    protected fun position(
        propertyName: String,
        width: Int,
        height: Int,
        default: () -> Point,
    ): ManagedOption<HudMeta> {
        val label = Text.translatable("firmament.config.${name}.${propertyName}")
        return option(propertyName, {
            val p = default()
            HudMeta(HudPosition(p.x, p.y, 1F), label, width, height)
        }, HudMetaHandler(this, label, width, height))
    }

    protected fun keyBinding(
        propertyName: String,
        default: () -> Int,
    ): ManagedOption<SavedKeyBinding> = keyBindingWithDefaultModifiers(propertyName) { SavedKeyBinding(default()) }

    protected fun keyBindingWithDefaultModifiers(
        propertyName: String,
        default: () -> SavedKeyBinding,
    ): ManagedOption<SavedKeyBinding> {
        return option(propertyName, default, KeyBindingHandler("firmament.config.${name}.${propertyName}", this))
    }

    protected fun integer(
        propertyName: String,
        min: Int,
        max: Int,
        default: () -> Int,
    ): ManagedOption<Int> {
        return option(propertyName, default, IntegerHandler(this, min, max))
    }

    protected fun button(propertyName: String, runnable: () -> Unit): ManagedOption<Unit> {
        return option(propertyName, { }, ClickHandler(this, runnable))
    }

    protected fun string(propertyName: String, default: () -> String): ManagedOption<String> {
        return option(propertyName, default, StringHandler(this))
    }


    fun reloadGui() {
        latestGuiAppender?.reloadables?.forEach { it() }
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
                    if (parent != null) {
                        setScreenLater(parent)
                    } else {
                        AllConfigsGui.showAllGuis()
                    }
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

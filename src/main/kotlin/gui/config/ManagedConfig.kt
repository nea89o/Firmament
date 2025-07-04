package moe.nea.firmament.gui.config

import com.mojang.serialization.Codec
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.gui.CloseEventListener
import io.github.notenoughupdates.moulconfig.gui.GuiComponentWrapper
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.component.CenterComponent
import io.github.notenoughupdates.moulconfig.gui.component.ColumnComponent
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.ScrollPanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import moe.nea.jarvis.api.Point
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.ScreenUtil.setScreenLater
import moe.nea.firmament.util.collections.InstanceList

abstract class ManagedConfig(
	override val name: String,
	val category: Category,
	// TODO: allow vararg secondaryCategories: Category,
) : ManagedConfigElement() {
	enum class Category {
		// Böse Kategorie, nicht benutzten lol
		MISC,
		CHAT,
		INVENTORY,
		ITEMS,
		MINING,
		GARDEN,
		EVENTS,
		INTEGRATIONS,
		META,
		DEV,
		;

		val labelText: Text = Text.translatable("firmament.config.category.${name.lowercase()}")
		val description: Text = Text.translatable("firmament.config.category.${name.lowercase()}.description")
		val configs: MutableList<ManagedConfig> = mutableListOf()
	}

	companion object {
		val allManagedConfigs = InstanceList<ManagedConfig>("ManagedConfig")
	}

	interface OptionHandler<T : Any> {
		fun initOption(opt: ManagedOption<T>) {}
		fun toJson(element: T): JsonElement?
		fun fromJson(element: JsonElement): T
		fun emitGuiElements(opt: ManagedOption<T>, guiAppender: GuiAppender)
	}

	init {
		allManagedConfigs.getAll().forEach {
			require(it.name != name) { "Duplicate name '$name' used for config" }
		}
		allManagedConfigs.add(this)
		category.configs.add(this)
	}

	// TODO: warn if two files use the same config file name :(
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
			it.handler.initOption(it)
			it.load(data)
			allOptions[propertyName] = it
			sortedOptions.add(it)
		}
	}

	protected fun toggle(propertyName: String, default: () -> Boolean): ManagedOption<Boolean> {
		return option(propertyName, default, BooleanHandler(this))
	}

	protected fun colour(propertyName: String, default: ()-> ChromaColour) : ManagedOption<ChromaColour> {
		return option(propertyName, default, ColourHandler(this))
	}

	protected fun <E> choice(
		propertyName: String,
		enumClass: Class<E>,
		default: () -> E
	): ManagedOption<E> where E : Enum<E>, E : StringIdentifiable {
		return option(propertyName, default, ChoiceHandler(enumClass, enumClass.enumConstants.toList()))
	}

	protected inline fun <reified E> choice(
		propertyName: String,
		noinline default: () -> E
	): ManagedOption<E> where E : Enum<E>, E : StringIdentifiable {
		return choice(propertyName, E::class.java, default)
	}

	private fun <E> createStringIdentifiable(x: () -> Array<out E>): Codec<E> where E : Enum<E>, E : StringIdentifiable {
		return StringIdentifiable.createCodec { x() }
	}

	// TODO: wait on https://youtrack.jetbrains.com/issue/KT-73434
//	protected inline fun <reified E> choice(
//		propertyName: String,
//		noinline default: () -> E
//	): ManagedOption<E>  where E : Enum<E>, E : StringIdentifiable {
//		return choice(
//			propertyName,
//			enumEntries<E>().toList(),
//			StringIdentifiable.createCodec { enumValues<E>() },
//			EnumRenderer.default(),
//			default
//		)
//	}
	open fun onChange(option: ManagedOption<*>) {
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
	): ManagedOption<SavedKeyBinding> = keyBindingWithOutDefaultModifiers(propertyName) { SavedKeyBinding(default()) }

	protected fun keyBindingWithOutDefaultModifiers(
		propertyName: String,
		default: () -> SavedKeyBinding,
	): ManagedOption<SavedKeyBinding> {
		return option(propertyName, default, KeyBindingHandler("firmament.config.${name}.${propertyName}", this))
	}

	protected fun keyBindingWithDefaultUnbound(
		propertyName: String,
	): ManagedOption<SavedKeyBinding> {
		return keyBindingWithOutDefaultModifiers(propertyName) { SavedKeyBinding(GLFW.GLFW_KEY_UNKNOWN) }
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

	val translationKey get() = "firmament.config.${name}"
	val labelText: Text = Text.translatable(translationKey)

	fun getConfigEditor(parent: Screen? = null): Screen {
		var screen: Screen? = null
		val guiapp = GuiAppender(400) { requireNotNull(screen) { "Screen Accessor called too early" } }
		latestGuiAppender = guiapp
		guiapp.appendFullRow(RowComponent(
			FirmButtonComponent(TextComponent("←")) {
				if (parent != null) {
					save()
					setScreenLater(parent)
				} else {
					AllConfigsGui.showAllGuis()
				}
			}
		))
		sortedOptions.forEach { it.appendToGui(guiapp) }
		guiapp.reloadables.forEach { it() }
		val component = CenterComponent(PanelComponent(ScrollPanelComponent(400, 300, ColumnComponent(guiapp.panel)),
		                                               10,
		                                               PanelComponent.DefaultBackgroundRenderer.VANILLA))
		screen = object : GuiComponentWrapper(GuiContext(component)) {
			override fun close() {
				if (context.onBeforeClose() == CloseEventListener.CloseAction.NO_OBJECTIONS_TO_CLOSE) {
					client!!.setScreen(parent)
				}
			}
		}
		return screen
	}

	fun showConfigEditor(parent: Screen? = null) {
		setScreenLater(getConfigEditor(parent))
	}

}

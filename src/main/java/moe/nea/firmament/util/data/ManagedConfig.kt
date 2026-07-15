package moe.nea.firmament.util.data

import com.mojang.serialization.Codec
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.gui.CloseEventListener
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.component.CenterComponent
import io.github.notenoughupdates.moulconfig.gui.component.ColumnComponent
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.ScrollPanelComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ChoiceHandler
import moe.nea.firmament.gui.config.ClickHandler
import moe.nea.firmament.gui.config.ColourHandler
import moe.nea.firmament.gui.config.DurationHandler
import moe.nea.firmament.gui.config.GuiAppender
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.gui.config.HudMetaHandler
import moe.nea.firmament.gui.config.HudPosition
import moe.nea.firmament.gui.config.IntegerHandler
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.gui.config.StringHandler
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.collections.InstanceList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.StringRepresentable
import org.joml.Vector2i
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.putJsonObject
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import moe.nea.firmament.gui.config.storage.ConfigStorageClass

abstract class ManagedConfig(
	val name: String,
	val category: Category,
) : IDataHolder<Unit>() {
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

		val labelText: Component = Component.translatable("firmament.config.category.${name.lowercase()}")
		val description: Component = Component.translatable("firmament.config.category.${name.lowercase()}.description")
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

	override fun keys(): Collection<Unit> {
		return listOf(Unit)
	}

	override fun clear() {
		sortedOptions.forEach {
			it._actualValue = null
		}
	}

	override val storageClass: ConfigStorageClass
		get() = ConfigStorageClass.CONFIG

	override fun saveTo(key: Unit): JsonObject {
		return buildJsonObject {
			putJsonObject(name) {
				sortedOptions.forEach {
					put(it.propertyName, it.toJson() ?: return@forEach)
				}
			}
		}
	}

	override fun explicitDefaultLoad() {
		val empty = JsonObject(mapOf())
		sortedOptions.forEach { it.load(empty) }
	}

	override fun loadFrom(key: Unit, jsonObject: JsonObject) {
		val unprefixed = jsonObject[name]?.jsonObject ?: JsonObject(mapOf())
		sortedOptions.forEach {
			it.load(unprefixed)
		}
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
			allOptions[propertyName] = it
			sortedOptions.add(it)
		}
	}

	protected fun toggle(propertyName: String, default: () -> Boolean): ManagedOption<Boolean> {
		return option(propertyName, default, BooleanHandler(this))
	}

	protected fun colour(propertyName: String, default: () -> ChromaColour): ManagedOption<ChromaColour> {
		return option(propertyName, default, ColourHandler(this))
	}

	protected fun <E> choice(
		propertyName: String,
		enumClass: Class<E>,
		default: () -> E
	): ManagedOption<E> where E : Enum<E>, E : StringRepresentable {
		return option(propertyName, default, ChoiceHandler(enumClass, enumClass.enumConstants.toList()))
	}

	protected inline fun <reified E> choice(
		propertyName: String,
		noinline default: () -> E
	): ManagedOption<E> where E : Enum<E>, E : StringRepresentable {
		return choice(propertyName, E::class.java, default)
	}

	private fun <E> createStringIdentifiable(x: () -> Array<out E>): Codec<E> where E : Enum<E>, E : StringRepresentable {
		return StringRepresentable.fromEnum { x() }
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
		default: () -> Vector2i,
	): ManagedOption<HudMeta> {
		val label = Component.translatable("firmament.config.${name}.${propertyName}")
		return option(propertyName, {
			val p = default()
			HudMeta(HudPosition(p.x(), p.y(), 1F), Firmament.identifier(propertyName), label, width, height)
		}, HudMetaHandler(this, propertyName, label, width, height))
	}

	protected fun keyBinding(
		propertyName: String,
		default: () -> Int,
	): ManagedOption<SavedKeyBinding> = keyBindingWithOutDefaultModifiers(propertyName) {
		SavedKeyBinding.Companion.keyWithoutMods(default())
	}

	protected fun keyBindingWithOutDefaultModifiers(
		propertyName: String,
		default: () -> SavedKeyBinding,
	): ManagedOption<SavedKeyBinding> {
		return option(propertyName, default, KeyBindingHandler("firmament.config.${name}.${propertyName}", this))
	}

	protected fun keyBindingWithDefaultUnbound(
		propertyName: String,
	): ManagedOption<SavedKeyBinding> {
		return keyBindingWithOutDefaultModifiers(propertyName) { SavedKeyBinding.Companion.unbound() }
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
	val labelText: Component = Component.translatable(translationKey)

	fun getConfigEditor(parent: Screen? = null): Screen {
		var screen: Screen? = null
		val guiapp = GuiAppender(400) { requireNotNull(screen) { "Screen Accessor called too early" } }
		latestGuiAppender = guiapp
		guiapp.appendFullRow(
			RowComponent(
				FirmButtonComponent(TextComponent("←")) {
					if (parent != null) {
						markDirty()
						ScreenUtil.setScreenLater(parent)
					} else {
						AllConfigsGui.showAllGuis()
					}
				}
			))
		sortedOptions.forEach { it.appendToGui(guiapp) }
		guiapp.reloadables.forEach { it() }
		val component = CenterComponent(
			PanelComponent(
				ScrollPanelComponent(400, 300, ColumnComponent(guiapp.panel)),
				10,
				PanelComponent.DefaultBackgroundRenderer.VANILLA
			)
		)
		screen = object : MoulConfigScreenComponent(Component.empty(), GuiContext(component), parent) {
			override fun onClose() {
				if (guiContext.onBeforeClose() == CloseEventListener.CloseAction.NO_OBJECTIONS_TO_CLOSE) {
					minecraft.gui.setScreen(parent)
				}
			}
		}
		return screen
	}

	fun showConfigEditor(parent: Screen? = null) {
		ScreenUtil.setScreenLater(getConfigEditor(parent))
	}

}

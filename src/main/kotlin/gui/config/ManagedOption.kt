package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import net.minecraft.text.Text
import moe.nea.firmament.util.ErrorUtil

class ManagedOption<T : Any>(
	val element: ManagedConfig,
	val propertyName: String,
	val default: () -> T,
	val handler: ManagedConfig.OptionHandler<T>
) : ReadWriteProperty<Any?, T>, GetSetter<T> {
	override fun set(newValue: T) {
		this.value = newValue
	}

	override fun get(): T {
		return this.value
	}

	val rawLabelText = "firmament.config.${element.name}.${propertyName}"
	val labelText: Text = Text.translatable(rawLabelText)
	val descriptionTranslationKey = "firmament.config.${element.name}.${propertyName}.description"
	val labelDescription: Text = Text.translatable(descriptionTranslationKey)

	private var actualValue: T? = null
	var value: T
		get() = actualValue ?: error("Lateinit variable not initialized")
		set(value) {
			actualValue = value
			element.onChange(this)
		}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		this.value = value
	}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return value
	}

	fun load(root: JsonElement) {
		if (root is JsonObject && root.containsKey(propertyName)) {
			try {
				value = handler.fromJson(root[propertyName]!!)
				return
			} catch (e: Exception) {
				ErrorUtil.logError(
					"Exception during loading of config file ${element.name}. This will reset this config.",
					e
				)
			}
		}
		value = default()
	}

	fun toJson(): JsonElement? {
		return handler.toJson(value)
	}

	fun appendToGui(guiapp: GuiAppender) {
		handler.emitGuiElements(this, guiapp)
	}
}

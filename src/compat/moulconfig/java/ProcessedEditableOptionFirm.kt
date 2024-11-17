package moe.nea.firmament.compat.moulconfig

import io.github.notenoughupdates.moulconfig.Config
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.util.ErrorUtil

abstract class ProcessedEditableOptionFirm<T : Any>(
	val managedOption: ManagedOption<T>,
	categoryAccordionId: Int,
	configObject: Config,
) : ProcessedOptionFirm(categoryAccordionId, configObject) {
	val managedConfig = managedOption.element
	override fun getDebugDeclarationLocation(): String {
		return "FirmamentOption:${managedConfig.name}:${managedOption.propertyName}"
	}

	override fun getName(): String {
		return managedOption.labelText.string
	}

	override fun getDescription(): String {
		return managedOption.labelDescription.string
	}

	abstract fun fromT(t: T): Any
	abstract fun toT(any: Any?): T?

	final override fun get(): Any {
		return fromT(managedOption.value)
	}

	final override fun set(p0: Any?): Boolean {
		managedOption.value = toT(p0) ?: run {
			ErrorUtil.softError("Failed to set value p0 in $this")
			return false
		}
		managedConfig.save()
		return true
	}

	override fun explicitNotifyChange() {
		managedConfig.save()
	}
}

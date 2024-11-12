package moe.nea.firmament.compat.moulconfig

import io.github.notenoughupdates.moulconfig.Config
import moe.nea.firmament.gui.config.ManagedOption

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

	override fun explicitNotifyChange() {
		managedConfig.save()
	}
}

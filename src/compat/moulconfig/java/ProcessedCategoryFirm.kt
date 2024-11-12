package moe.nea.firmament.compat.moulconfig

import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorAccordion
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import moe.nea.firmament.gui.config.ManagedConfig

class ProcessedCategoryFirm(
	val category: ManagedConfig.Category,
	private val options: List<ProcessedOptionFirm>
) : ProcessedCategory {
	val accordions = options.filter { it.editor is GuiOptionEditorAccordion }
		.associateBy { (it.editor as GuiOptionEditorAccordion).accordionId }
	init {
		for (option in options) {
			option.category = this
		}
	}

	override fun getDebugDeclarationLocation(): String? {
		return "FirmamentCategory.${category.name}"
	}

	override fun getDisplayName(): String {
		return category.labelText.string
	}

	override fun getDescription(): String {
		return "Missing description" // TODO: add description
	}

	override fun getIdentifier(): String {
		return category.name
	}

	override fun getParentCategoryId(): String? {
		return null
	}

	override fun getOptions(): List<ProcessedOption> {
		return options
	}

	override fun getAccordionAnchors(): Map<Int, ProcessedOption> {
		return accordions
	}
}

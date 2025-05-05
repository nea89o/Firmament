package moe.nea.firmament.compat.moulconfig

import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.SearchTag
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption

abstract class ProcessedOptionFirm(
	private val accordionId: Int,
	private val config: Config
) : ProcessedOption {
	override fun getPath(): String? {
		return "nonsense"
	}
	lateinit var category: ProcessedCategoryFirm
	override fun getAccordionId(): Int {
		return accordionId
	}

	protected abstract fun createEditor(): GuiOptionEditor
	val editorInstance by lazy { createEditor() }

	override fun getSearchTags(): Array<SearchTag> {
		return emptyArray()
	}

	override fun getEditor(): GuiOptionEditor {
		return editorInstance
	}

	override fun getCategory(): ProcessedCategory {
		return category
	}

	override fun getConfig(): Config {
		return config
	}

	override fun explicitNotifyChange() {
	}
}

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.gui.HorizontalAlign
import io.github.notenoughupdates.moulconfig.gui.VerticalAlign
import io.github.notenoughupdates.moulconfig.gui.component.AlignComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.optionals.getOrNull
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.gui.CheckboxComponent
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.json.KJsonOps

class ChoiceHandler<E>(
	val enumClass: Class<E>,
	val universe: List<E>,
) : ManagedConfig.OptionHandler<E> where E : Enum<E>, E : StringIdentifiable {
	val codec = StringIdentifiable.createCodec {
		@Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
		(universe as java.util.List<*>).toArray(arrayOfNulls<Enum<E>>(0)) as Array<E>
	}
	val renderer = EnumRenderer.default<E>()

	override fun toJson(element: E): JsonElement? {
		return codec.encodeStart(KJsonOps.INSTANCE, element)
			.promotePartial { ErrorUtil.softError("Failed to encode json element '$element': $it") }.result()
			.getOrNull()
	}

	override fun fromJson(element: JsonElement): E {
		return codec.decode(KJsonOps.INSTANCE, element)
			.promotePartial { ErrorUtil.softError("Failed to decode json element '$element': $it") }
			.result()
			.get()
			.first
	}

	override fun emitGuiElements(opt: ManagedOption<E>, guiAppender: GuiAppender) {
		guiAppender.appendFullRow(TextComponent(opt.labelText.string))
		for (e in universe) {
			guiAppender.appendFullRow(RowComponent(
				AlignComponent(CheckboxComponent(opt, e), { HorizontalAlign.LEFT }, { VerticalAlign.CENTER }),
				TextComponent(renderer.getName(opt, e).string)
			))
		}
	}
}

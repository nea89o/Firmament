package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.gui.component.ColorSelectComponent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class ColourHandler(val config: ManagedConfig) :
	ManagedConfig.OptionHandler<ChromaColour> {
	@Serializable
	data class ChromaDelegate(
		@SerialName("h")
		val hue: Float,
		@SerialName("s")
		val saturation: Float,
		@SerialName("b")
		val brightness: Float,
		@SerialName("a")
		val alpha: Int,
		@SerialName("c")
		val timeForFullRotationInMillis: Int,
	) {
		constructor(delegate: ChromaColour) : this(
			delegate.hue,
			delegate.saturation,
			delegate.brightness,
			delegate.alpha,
			delegate.timeForFullRotationInMillis
		)

		fun into(): ChromaColour = ChromaColour(hue, saturation, brightness, alpha, timeForFullRotationInMillis)
	}

	object ChromaSerializer : KSerializer<ChromaColour> {
		override val descriptor: SerialDescriptor
			get() = SerialDescriptor("FirmChromaColour", ChromaDelegate.serializer().descriptor)

		override fun serialize(
			encoder: Encoder,
			value: ChromaColour
		) {
			encoder.encodeSerializableValue(ChromaDelegate.serializer(), ChromaDelegate(value))
		}

		override fun deserialize(decoder: Decoder): ChromaColour {
			return decoder.decodeSerializableValue(ChromaDelegate.serializer()).into()
		}
	}

	override fun toJson(element: ChromaColour): JsonElement? {
		return Json.encodeToJsonElement(ChromaSerializer, element)
	}

	override fun fromJson(element: JsonElement): ChromaColour {
		return Json.decodeFromJsonElement(ChromaSerializer, element)
	}

	override fun emitGuiElements(
		opt: ManagedOption<ChromaColour>,
		guiAppender: GuiAppender
	) {
		guiAppender.appendLabeledRow(
			opt.labelText,
			ColorSelectComponent(
				0,
				0,
				opt.value.toLegacyString(),
				{
					opt.value = ChromaColour.forLegacyString(it)
					config.save()
				},
				{ }
			)
		)
	}
}

package moe.nea.firmament.features.debug

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional

data class ExportedTestConstantMeta(
	val dataVersion: Int,
	val modVersion: Optional<String>,
) {
	companion object {
		val CODEC: Codec<ExportedTestConstantMeta> = RecordCodecBuilder.create {
			it.group(
				Codec.INT.fieldOf("dataVersion").forGetter(ExportedTestConstantMeta::dataVersion),
				Codec.STRING.optionalFieldOf("modVersion").forGetter(ExportedTestConstantMeta::modVersion),
			).apply(it, ::ExportedTestConstantMeta)
		}
	}
}

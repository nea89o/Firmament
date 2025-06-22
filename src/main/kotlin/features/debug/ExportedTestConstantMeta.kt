package moe.nea.firmament.features.debug

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.SharedConstants
import moe.nea.firmament.Firmament

data class ExportedTestConstantMeta(
	val dataVersion: Int,
	val modVersion: Optional<String>,
) {
	companion object {
		val current = ExportedTestConstantMeta(
			SharedConstants.getGameVersion().saveVersion.id,
			Optional.of("Firmament ${Firmament.version.friendlyString}")
		)

		val CODEC: Codec<ExportedTestConstantMeta> = RecordCodecBuilder.create {
			it.group(
				Codec.INT.fieldOf("dataVersion").forGetter(ExportedTestConstantMeta::dataVersion),
				Codec.STRING.optionalFieldOf("modVersion").forGetter(ExportedTestConstantMeta::modVersion),
			).apply(it, ::ExportedTestConstantMeta)
		}
		val SOURCE_CODEC = CODEC.fieldOf("source").codec()
	}
}


package moe.nea.firmament.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.repo.RepoManager

@Serializable(with = SkyBlockIsland.Serializer::class)
class SkyBlockIsland
private constructor(
    val locrawMode: String,
) {

    object Serializer : KSerializer<SkyBlockIsland> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("SkyBlockIsland", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SkyBlockIsland {
            return forMode(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: SkyBlockIsland) {
            encoder.encodeString(value.locrawMode)
        }
    }
    companion object {
        private val allIslands = mutableMapOf<String, SkyBlockIsland>()
        fun forMode(mode: String): SkyBlockIsland = allIslands.computeIfAbsent(mode, ::SkyBlockIsland)
        val HUB = forMode("hub")
        val PRIVATE_ISLAND = forMode("dynamic")
        val RIFT = forMode("rift")
        val MINESHAFT = forMode("mineshaft")
	    val GARDEN = forMode("garden")
	    val DUNGEON = forMode("dungeon")
    }

    val userFriendlyName
        get() = RepoManager.neuRepo.constants.islands.areaNames
            .getOrDefault(locrawMode, locrawMode)
}

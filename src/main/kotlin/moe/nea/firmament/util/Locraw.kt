

package moe.nea.firmament.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Locraw(val server: String, val gametype: String? = null, val mode: String? = null, val map: String? = null) {
    @Transient
    val skyblockLocation = if (gametype == "SKYBLOCK") mode?.let(SkyBlockIsland::forMode) else null
}

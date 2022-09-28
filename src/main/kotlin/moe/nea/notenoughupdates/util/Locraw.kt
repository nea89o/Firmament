package moe.nea.notenoughupdates.util

import kotlinx.serialization.Serializable

@Serializable
data class Locraw(val server: String, val gametype: String? = null, val mode: String? = null, val map: String? = null) {
    val skyblockLocation = if (gametype == "SKYBLOCK") mode else null
}

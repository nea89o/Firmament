package moe.nea.notenoughupdates.util

import dev.architectury.event.events.client.ClientPlayerEvent
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.events.ServerChatLineReceivedEvent
import moe.nea.notenoughupdates.events.SkyblockServerUpdateEvent

@OptIn(ExperimentalTime::class)
object SBData {
    val profileRegex = "(?:Your profile was changed to: |You are playing on profile: )(.+)".toRegex()
    var profileCuteName: String? = null

    private var lastLocrawSent: TimeSource.Monotonic.ValueTimeMark? = null
    private val locrawRoundtripTime: Duration = 5.seconds
    var locraw: Locraw? = null
    val skyblockLocation get() = locraw?.skyblockLocation


    fun init() {
        ServerChatLineReceivedEvent.subscribe { event ->
            val profileMatch = profileRegex.matchEntire(event.unformattedString)
            if (profileMatch != null) {
                profileCuteName = profileMatch.groupValues[1]
            }
            if (event.unformattedString.startsWith("{")) {
                val lLS = lastLocrawSent
                if (tryReceiveLocraw(event.unformattedString) && lLS != null && lLS.elapsedNow() < locrawRoundtripTime) {
                    lastLocrawSent = null
                    event.cancel()
                }
            }
        }

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ClientPlayerEvent.ClientPlayerJoin {
            locraw = null
            sendLocraw()
        })
    }

    private fun tryReceiveLocraw(unformattedString: String): Boolean = try {
        val lastLocraw = locraw
        locraw = NotEnoughUpdates.json.decodeFromString<Locraw>(unformattedString)
        SkyblockServerUpdateEvent.publish(SkyblockServerUpdateEvent(lastLocraw, locraw))
        true
    } catch (e: SerializationException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }

    fun sendLocraw() {
        lastLocrawSent = TimeSource.Monotonic.markNow()
        MC.player?.sendCommand("locraw")
    }


}

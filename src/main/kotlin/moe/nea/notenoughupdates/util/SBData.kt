package moe.nea.notenoughupdates.util

import java.time.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import net.minecraft.network.message.ArgumentSignatureDataMap
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.events.ServerChatLineReceivedEvent
import moe.nea.notenoughupdates.events.SkyblockServerUpdateEvent
import moe.nea.notenoughupdates.events.WorldReadyEvent

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
                    // event.cancel()
                }
            }
        }

        WorldReadyEvent.subscribe {
            sendLocraw()
            locraw = null
        }
    }

    private fun tryReceiveLocraw(unformattedString: String): Boolean = try {
        val lastLocraw = locraw
        locraw = NotEnoughUpdates.json.decodeFromString<Locraw>(unformattedString)
        SkyblockServerUpdateEvent.publish(SkyblockServerUpdateEvent(lastLocraw, locraw))
        true
    } catch (e: SerializationException) {
        e.printStackTrace()
        false
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        false
    }

    fun sendLocraw() {
        lastLocrawSent = TimeSource.Monotonic.markNow()
        val nh = MC.player?.networkHandler ?: return
        val ack = nh.consumeAcknowledgment()
        nh.sendPacket(
            CommandExecutionC2SPacket(
                "locraw",
                Instant.now(),
                0L,
                ArgumentSignatureDataMap.EMPTY,
                false,
                ack
            )
        )
    }


}

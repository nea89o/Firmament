/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import java.util.UUID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.ClientChatLineReceivedEvent
import moe.nea.firmament.events.OutgoingPacketEvent
import moe.nea.firmament.events.ServerChatLineReceivedEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.events.WorldReadyEvent

object SBData {
    private val profileRegex = "Profile ID: ([a-z0-9\\-]+)".toRegex()
    var profileId: UUID? = null

    private var lastLocrawSent = Timer()
    private val anyLocrawSent = Timer()
    private val locrawRoundtripTime: Duration = 5.seconds
    private var hasReceivedProfile = false
    private var hasSentLocraw = false
    var locraw: Locraw? = null
    val skyblockLocation: String? get() = locraw?.skyblockLocation
    val hasValidLocraw get() = locraw?.server !in listOf("limbo", null)

    fun init() {
        OutgoingPacketEvent.subscribe { event ->
            if (event.packet is CommandExecutionC2SPacket && event.packet.command == "locraw") {
                anyLocrawSent.markNow()
            }
        }
        ServerChatLineReceivedEvent.subscribe { event ->
            val profileMatch = profileRegex.matchEntire(event.unformattedString)
            if (profileMatch != null) {
                try {
                    profileId = UUID.fromString(profileMatch.groupValues[1])
                    hasReceivedProfile = true
                    if (!hasValidLocraw && !hasSentLocraw && anyLocrawSent.timePassed() > locrawRoundtripTime) {
                        sendLocraw()
                    }
                } catch (e: IllegalArgumentException) {
                    profileId = null
                    e.printStackTrace()
                }
            }
            if (event.unformattedString.startsWith("{")) {
                if (tryReceiveLocraw(event.unformattedString)) {
                    if (!hasValidLocraw && !hasSentLocraw && hasReceivedProfile) {
                        sendLocraw()
                    }
                }
            }
        }
        ClientChatLineReceivedEvent.subscribe { event ->
            if (event.unformattedString.startsWith("{") && tryReceiveLocraw(event.unformattedString) && lastLocrawSent.timePassed() < locrawRoundtripTime) {
                lastLocrawSent.markFarPast()
                event.cancel()
            }
        }

        WorldReadyEvent.subscribe {
            locraw = null
            hasSentLocraw = false
            hasReceivedProfile = false
        }
    }

    private fun tryReceiveLocraw(unformattedString: String, update: Boolean = true): Boolean = try {
        val lastLocraw = locraw
        val n = Firmament.json.decodeFromString<Locraw>(unformattedString)
        if (update) {
            locraw = n
            SkyblockServerUpdateEvent.publish(SkyblockServerUpdateEvent(lastLocraw, locraw))
        }
        true
    } catch (e: SerializationException) {
        e.printStackTrace()
        false
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        false
    }

    fun sendLocraw() {
        hasSentLocraw = true
        lastLocrawSent.markNow()
        val nh = MC.player?.networkHandler ?: return
        nh.sendChatCommand("locraw")
    }


}

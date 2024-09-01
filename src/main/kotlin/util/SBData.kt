package moe.nea.firmament.util

import java.util.UUID
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds
import moe.nea.firmament.events.AllowChatEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.ServerConnectedEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.events.WorldReadyEvent

object SBData {
    private val profileRegex = "Profile ID: ([a-z0-9\\-]+)".toRegex()
    val profileSuggestTexts = listOf(
        "CLICK THIS TO SUGGEST IT IN CHAT [DASHES]",
        "CLICK THIS TO SUGGEST IT IN CHAT [NO DASHES]",
    )
    var profileId: UUID? = null

    private var hasReceivedProfile = false
    var locraw: Locraw? = null
    val skyblockLocation: SkyBlockIsland? get() = locraw?.skyblockLocation
    val hasValidLocraw get() = locraw?.server !in listOf("limbo", null)
    val isOnSkyblock get() = locraw?.gametype == "SKYBLOCK"
    var lastProfileIdRequest = TimeMark.farPast()
    fun init() {
        ServerConnectedEvent.subscribe("SBData:onServerConnected") {
            HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)
        }
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket::class.java) {
            MC.onMainThread {
                val lastLocraw = locraw
                locraw = Locraw(it.serverName,
                                it.serverType.getOrNull()?.name?.uppercase(),
                                it.mode.getOrNull(),
                                it.map.getOrNull())
                SkyblockServerUpdateEvent.publish(SkyblockServerUpdateEvent(lastLocraw, null))
            }
        }
        SkyblockServerUpdateEvent.subscribe("SBData:sendProfileId") {
            if (!hasReceivedProfile && isOnSkyblock && lastProfileIdRequest.passedTime() > 30.seconds) {
                lastProfileIdRequest = TimeMark.now()
                MC.sendServerCommand("profileid")
            }
        }
        AllowChatEvent.subscribe("SBData:hideProfileSuggest") { event ->
            if (event.unformattedString in profileSuggestTexts && lastProfileIdRequest.passedTime() < 5.seconds) {
                event.cancel()
            }
        }
        ProcessChatEvent.subscribe(receivesCancelled = true, "SBData:loadProfile") { event ->
            val profileMatch = profileRegex.matchEntire(event.unformattedString)
            if (profileMatch != null) {
                try {
                    profileId = UUID.fromString(profileMatch.groupValues[1])
                    hasReceivedProfile = true
                } catch (e: IllegalArgumentException) {
                    profileId = null
                    e.printStackTrace()
                }
            }
        }
    }
}

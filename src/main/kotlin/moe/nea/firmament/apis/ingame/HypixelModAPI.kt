
package moe.nea.firmament.apis.ingame

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.apis.ingame.packets.PartyInfoRequest
import moe.nea.firmament.apis.ingame.packets.PartyInfoResponse
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.FirmamentCustomPayloadEvent
import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.util.MC


object HypixelModAPI : SubscriptionOwner {
    init {
        InGameCodecWrapper.Direction.C2S.customCodec =
            InGameCodecWrapper.createStealthyCodec(
                PartyInfoRequest.intoType()
            )
        InGameCodecWrapper.Direction.S2C.customCodec =
            InGameCodecWrapper.createStealthyCodec(
                PartyInfoResponse.intoType()
            )
    }

    @JvmStatic
    fun sendRequest(packet: FirmamentCustomPayload) {
        MC.networkHandler?.sendPacket(CustomPayloadC2SPacket(packet))
    }

    @Subscribe
    fun testCommand(event: CommandEvent.SubCommand) {
        event.subcommand("sendpartyrequest") {
            thenExecute {
                sendRequest(PartyInfoRequest(1))
            }
        }
    }

    @Subscribe
    fun logEvents(event: FirmamentCustomPayloadEvent) {
        MC.sendChat(Text.stringifiedTranslatable("firmament.modapi.event", event.toString()))
    }

    override val delegateFeature: FirmamentFeature
        get() = DeveloperFeatures
}

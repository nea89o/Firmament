package moe.nea.firmament.features.misc

import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.JoinServerEvent

/**
 * This is a class that announces all mods to hypixel (or any other server you connect to).
 *
 * I don't mind cheat mods, i just think they should be honest about themselves.
 * I AM A RETARDED TRANNY FURRYFAG sorry
 
 * If you are a cheat mod (or think you could possibly be seen as one), you have two options:
 *
 * - risk getting your users banned from hypixel by exposing your modid + version number in a packet sent on first connect
 * - hide yourself from this list by either a mixin, or (more easily) by specifying a tag in your fabric.mod.json:
 *
 * ```json
 * {
 *   "schemaVersion": 1,
 *   "id": "my-cheat-mod",
 *   "custom": { "firmament:hide_from_modlist": true }
 * }
 * ```
 */
object ModAnnouncer {

	data class ModEntry(
		val modid: String,
		val modVersion: String,
	) {
		companion object {
			val CODEC: StreamCodec<ByteBuf, ModEntry> = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, ModEntry::modid,
				ByteBufCodecs.STRING_UTF8, ModEntry::modVersion,
				::ModEntry
			)
		}
	}

	data class ModPacket(
		val mods: List<ModEntry>,
	) : CustomPacketPayload {
		override fun type(): CustomPacketPayload.Type<out ModPacket> {
			return ID
		}

		companion object {
			val ID = CustomPacketPayload.Type<ModPacket>(Firmament.identifier("mod_list"))
			val CODEC: StreamCodec<ByteBuf, ModPacket> = ModEntry.CODEC.apply(ByteBufCodecs.list())
				.map(::ModPacket, ModPacket::mods)
		}
	}

	@Subscribe
	fun onServerJoin(event: JoinServerEvent) {
		val packet = ModPacket(
			FabricLoader.getInstance()
				.allMods
				.filter { !it.metadata.containsCustomValue("firmament:hide_from_modlist") }
				.map { ModEntry(it.metadata.id, it.metadata.version.friendlyString) })
		val pbb = PacketByteBufs.create()
		ModPacket.CODEC.encode(pbb, packet)
		if (pbb.writerIndex() > ServerboundCustomPayloadPacket.MAX_PAYLOAD_SIZE)
			return

		event.networkHandler.send(event.packetSender.createPacket(packet))
	}

	init {
		PayloadTypeRegistry.playC2S().register(ModPacket.ID, ModPacket.CODEC)
	}
}

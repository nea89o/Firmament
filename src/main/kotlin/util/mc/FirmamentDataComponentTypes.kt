package moe.nea.firmament.util.mc

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.component.ComponentType
import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientInitEvent
import moe.nea.firmament.repo.MiningRepoData

object FirmamentDataComponentTypes {

	@Subscribe
	fun init(event: ClientInitEvent) {
	}

	private fun <T> register(
		id: String,
		builderOperator: (ComponentType.Builder<T>) -> Unit
	): ComponentType<T> {
		return Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Firmament.identifier(id),
			ComponentType.builder<T>().also(builderOperator)
				.build()
		)
	}

	fun <T> errorCodec(message: String): PacketCodec<in ByteBuf, T> =
		object : PacketCodec<ByteBuf, T> {
			override fun decode(buf: ByteBuf?): T? {
				error(message)
			}

			override fun encode(buf: ByteBuf?, value: T?) {
				error(message)
			}
		}

	fun <T, B : ComponentType.Builder<T>> B.neverEncode(message: String = "This element should never be encoded or decoded"): B {
		packetCodec(errorCodec(message))
		codec(null)
		return this
	}

	val IS_BROKEN = register<Boolean>(
		"is_broken"
	) {
		it.codec(Codec.BOOL.fieldOf("is_broken").codec())
	}

	val CUSTOM_MINING_BLOCK_DATA = register<MiningRepoData.CustomMiningBlock>("custom_mining_block") {
		it.neverEncode()
	}


}

package moe.nea.firmament.util.mc

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.codec.StreamCodec
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientInitEvent
import moe.nea.firmament.repo.MiningRepoData

object FirmamentDataComponentTypes {

	@Subscribe
	fun init(event: ClientInitEvent) {
	}

	private fun <T : Any> register(
		id: String,
		builderOperator: (DataComponentType.Builder<T>) -> Unit
	): DataComponentType<T> {
		return Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Firmament.identifier(id),
			DataComponentType.builder<T>().also(builderOperator)
				.build()
		)
	}

	fun <T : Any> errorCodec(message: String): StreamCodec<in ByteBuf, T> =
		object : StreamCodec<ByteBuf, T> {
			override fun decode(buf: ByteBuf): T {
				error(message)
			}

			override fun encode(buf: ByteBuf, value: T) {
				error(message)
			}
		}

	fun <T : Any, B : DataComponentType.Builder<T>> B.neverEncode(message: String = "This element should never be encoded or decoded"): B {
		networkSynchronized(errorCodec(message))
		return this
	}

	val IS_BROKEN = register<Boolean>(
		"is_broken"
	) {
		it.persistent(Codec.BOOL.fieldOf("is_broken").codec())
	}

	val CUSTOM_MINING_BLOCK_DATA = register<MiningRepoData.CustomMiningBlock>("custom_mining_block") {
		it.neverEncode()
	}


}

package moe.nea.firmament.util.json

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Lifecycle
import com.mojang.util.UndashedUuid
import net.minecraft.util.Uuids

object FirmCodecs {
	@JvmField
	val UUID_LENIENT_PREFER_INT_STREAM = Codec.withAlternative(Uuids.INT_STREAM_CODEC, Codec.STRING.comapFlatMap(
		{
			try {
				DataResult.success(UndashedUuid.fromStringLenient(it), Lifecycle.stable())
			} catch (ex: IllegalArgumentException) {
				DataResult.error { "Invalid UUID $it: ${ex.message}" }
			}
		},
		UndashedUuid::toString))
}

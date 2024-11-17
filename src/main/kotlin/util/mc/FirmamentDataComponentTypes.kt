package moe.nea.firmament.util.mc

import com.mojang.serialization.Codec
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientInitEvent

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

	val IS_BROKEN = register<Boolean>(
		"is_broken"
	) {
		it.codec(Codec.BOOL.fieldOf("is_broken").codec())
	}


}

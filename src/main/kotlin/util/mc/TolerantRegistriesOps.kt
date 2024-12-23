package moe.nea.firmament.util.mc

import com.mojang.serialization.DynamicOps
import java.util.Optional
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryOwner

class TolerantRegistriesOps<T>(
	delegate: DynamicOps<T>,
	registryInfoGetter: RegistryInfoGetter
) : RegistryOps<T>(delegate, registryInfoGetter) {
	constructor(delegate: DynamicOps<T>, registry: RegistryWrapper.WrapperLookup) :
		this(delegate, CachedRegistryInfoGetter(registry))

	class TolerantOwner<E> : RegistryEntryOwner<E> {
		override fun ownerEquals(other: RegistryEntryOwner<E>?): Boolean {
			return true
		}
	}

	override fun <E : Any?> getOwner(registryRef: RegistryKey<out Registry<out E>>?): Optional<RegistryEntryOwner<E>> {
		return super.getOwner(registryRef).map {
			TolerantOwner()
		}
	}
}

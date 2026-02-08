package moe.nea.firmament.util.mc

import com.mojang.serialization.DynamicOps
import java.util.Optional
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.RegistryOps
import net.minecraft.core.HolderLookup
import net.minecraft.core.HolderOwner

class TolerantRegistriesOps<T : Any>(
	delegate: DynamicOps<T>,
	registryInfoGetter: RegistryInfoLookup
) : RegistryOps<T>(delegate, registryInfoGetter) {
	constructor(delegate: DynamicOps<T>, registry: HolderLookup.Provider) :
		this(delegate, HolderLookupAdapter(registry))

	class TolerantOwner<E : Any> : HolderOwner<E> {
		override fun canSerializeIn(other: HolderOwner<E>): Boolean {
			return true
		}
	}

	override fun <E : Any> owner(registryRef: ResourceKey<out Registry<out E>>): Optional<HolderOwner<E>> {
		return super.owner(registryRef).map {
			TolerantOwner()
		}
	}
}

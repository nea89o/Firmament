package moe.nea.firmament.compat.jade

import snownee.jade.api.Accessor
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.callback.JadeRayTraceCallback
import net.minecraft.world.phys.HitResult
import moe.nea.firmament.repo.MiningRepoData
import moe.nea.firmament.util.mc.FirmamentDataComponentTypes

class CustomFakeBlockProvider(val registration: IWailaClientRegistration) : JadeRayTraceCallback {

	override fun onRayTrace(hitResult: HitResult, accessor: Accessor<*>, originalAccessor: Accessor<*>): Accessor<*>?{
		if (!JadeIntegration.TConfig.blockDetection) return accessor
		if (accessor !is BlockAccessor) return accessor
		val customBlock = JadeIntegration.customBlocks[accessor.block] ?: return accessor
		return registration.blockAccessor()
			.from(accessor)
			.serversideRep(customBlock.getDisplayItem(accessor.block))
			.build()
	}

	companion object {
		@JvmStatic
		fun hasCustomBlock(accessor: BlockAccessor): Boolean {
			return getCustomBlock(accessor) != null
		}

		@JvmStatic
		fun getCustomBlock(accessor: BlockAccessor): MiningRepoData.CustomMiningBlock? {
			val item = accessor.serversideRep ?: return null
			return item.get(FirmamentDataComponentTypes.CUSTOM_MINING_BLOCK_DATA)
		}
	}
}

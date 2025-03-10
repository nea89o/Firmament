package moe.nea.firmament.compat.jade

import snownee.jade.api.Accessor
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.callback.JadeRayTraceCallback
import net.minecraft.util.hit.HitResult
import moe.nea.firmament.repo.MiningRepoData
import moe.nea.firmament.util.mc.FirmamentDataComponentTypes

class CustomFakeBlockProvider(val registration: IWailaClientRegistration) : JadeRayTraceCallback {

	override fun onRayTrace(
		hitResult: HitResult,
		accessor: Accessor<*>?,
		originalAccessor: Accessor<*>?
	): Accessor<*>? {
		if (!JadeIntegration.TConfig.blockDetection) return accessor
		if (accessor !is BlockAccessor) return accessor
		val customBlock = JadeIntegration.customBlocks[accessor.block]
		if (customBlock == null) return accessor
		return registration.blockAccessor()
			.from(accessor)
			.fakeBlock(customBlock.getDisplayItem(accessor.block))
			.build()
	}

	companion object {
		@JvmStatic
		fun hasCustomBlock(accessor: BlockAccessor): Boolean {
			return getCustomBlock(accessor) != null
		}

		@JvmStatic
		fun getCustomBlock(accessor: BlockAccessor): MiningRepoData.CustomMiningBlock? {
			if (!accessor.isFakeBlock) return null
			val item = accessor.fakeBlock
			return item.get(FirmamentDataComponentTypes.CUSTOM_MINING_BLOCK_DATA)
		}
	}
}

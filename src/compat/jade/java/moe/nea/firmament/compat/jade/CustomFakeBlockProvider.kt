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
		if (accessor !is BlockAccessor) return accessor
		val customBlock = CurrentCustomBlockHolder.customBlocks[accessor.block]
		if (customBlock == null) return accessor
		return registration.blockAccessor()
			.from(accessor)
			.fakeBlock(customBlock.getDisplayItem(accessor.block))
			.build()
	}

	companion object {
		fun getCustomBlock(accessor: BlockAccessor): MiningRepoData.CustomMiningBlock? {
			if (!accessor.isFakeBlock) return null
			val item = accessor.fakeBlock
			return item.get(FirmamentDataComponentTypes.CUSTOM_MINING_BLOCK_DATA)
		}
	}
}

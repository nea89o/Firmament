package moe.nea.firmament.compat.jade

import snownee.jade.addon.harvest.HarvestToolProvider
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import moe.nea.firmament.Firmament

// TODO: make this display breaking power, override names of mineable blocks, and override breaking indicator to use mining fatigue system
@WailaPlugin
class FirmamentJadePlugin : IWailaPlugin {
	override fun register(registration: IWailaCommonRegistration) {
		Firmament.logger.debug("Registering Jade integration...")
	}

	override fun registerClient(registration: IWailaClientRegistration) {
		registration.registerBlockComponent(CustomMiningHardnessProvider, Block::class.java)
		registration.registerProgressClient(SkyblockProgressProvider())
		registration.registerBlockComponent(DrillToolProvider(), Block::class.java)
		registration.addRayTraceCallback(CustomFakeBlockProvider(registration))
	}
}

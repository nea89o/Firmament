package moe.nea.firmament.compat.jade

import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import net.minecraft.block.Block
import moe.nea.firmament.Firmament

@WailaPlugin
class FirmamentJadePlugin : IWailaPlugin {
	override fun register(registration: IWailaCommonRegistration) {
		Firmament.logger.debug("Registering Jade integration...")
	}

	override fun registerClient(registration: IWailaClientRegistration) {
		registration.registerBlockComponent(CustomMiningHardnessProvider, Block::class.java)
		registration.registerBlockComponent(DrillToolProvider(), Block::class.java)
		registration.addRayTraceCallback(CustomFakeBlockProvider(registration))
	}
}

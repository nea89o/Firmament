package moe.nea.firmament.compat.jade

import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import net.minecraft.block.Blocks
import moe.nea.firmament.Firmament

// TODO: make this display breaking power, override names of mineable blocks, and override breaking indicator to use mining fatigue system
@WailaPlugin
class FirmamentJadePlugin : IWailaPlugin {
	override fun register(registration: IWailaCommonRegistration) {
		Firmament.logger.debug("Registering Jade integration...")
	}

	override fun registerClient(registration: IWailaClientRegistration) {
		registration.registerBlockComponent(MithrilProvider("prismarine"), Blocks.PRISMARINE::class.java)
		registration.registerBlockComponent(MithrilProvider("gray_wool"), Blocks.GRAY_WOOL::class.java)
		registration.registerBlockComponent(MithrilProvider("gray_concrete"), Blocks.GRAY_CONCRETE::class.java)
		// and together, we are the crystal gems of celeste minecraft (surely there's a better way to do this :sob:)
		registration.registerBlockComponent(GemstoneProvider("red_stained_glass", "ruby"), Blocks.RED_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("red_stained_glass_pane", "ruby"), Blocks.RED_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("purple_stained_glass", "amethyst"), Blocks.PURPLE_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("purple_stained_glass_pane", "amethyst"), Blocks.PURPLE_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("lime_stained_glass", "jade"), Blocks.LIME_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("lime_stained_glass_pane", "jade"), Blocks.LIME_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("blue_stained_glass", "sapphire"), Blocks.BLUE_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("blue_stained_glass_pane", "sapphire"), Blocks.BLUE_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("orange_stained_glass", "amber"), Blocks.ORANGE_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("orange_stained_glass_pane", "amber"), Blocks.ORANGE_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("yellow_stained_glass", "topaz"), Blocks.YELLOW_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("yellow_stained_glass_pane", "topaz"), Blocks.YELLOW_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("magenta_stained_glass", "jasper"), Blocks.MAGENTA_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("magenta_stained_glass_pane", "jasper"), Blocks.MAGENTA_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("white_stained_glass", "opal"), Blocks.WHITE_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("white_stained_glass_pane", "opal"), Blocks.WHITE_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("blue_stained_glass", "aquamarine"), Blocks.BLUE_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("blue_stained_glass_pane", "aquamarine"), Blocks.BLUE_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("black_stained_glass", "onyx"), Blocks.BLACK_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("black_stained_glass_pane", "onyx"), Blocks.BLACK_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("brown_stained_glass", "citrine"), Blocks.BROWN_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("brown_stained_glass_pane", "citrine"), Blocks.BROWN_STAINED_GLASS_PANE::class.java)
		registration.registerBlockComponent(GemstoneProvider("green_stained_glass", "peridot"), Blocks.GREEN_STAINED_GLASS::class.java)
		registration.registerBlockComponent(GemstoneProvider("green_stained_glass_pane", "peridot"), Blocks.GREEN_STAINED_GLASS_PANE::class.java)
		registration.registerProgressClient(SkyblockProgressProvider())
	}
}

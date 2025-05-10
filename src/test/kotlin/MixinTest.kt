package moe.nea.firmament.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.IMixinTransformer
import moe.nea.firmament.init.MixinPlugin

class MixinTest {
	@Test
	fun mixinAudit() {
		FirmTestBootstrap.bootstrapMinecraft()
		MixinEnvironment.getCurrentEnvironment().audit()
		val mp = MixinPlugin.instances.single()
		Assertions.assertEquals(
			mp.expectedFullPathMixins,
			mp.appliedFullPathMixins,
		)
		Assertions.assertNotEquals(
			0,
			mp.mixins.size
		)

	}

	@Test
	fun hasInstalledMixinTransformer() {
		Assertions.assertInstanceOf(
			IMixinTransformer::class.java,
			MixinEnvironment.getCurrentEnvironment().activeTransformer
		)
	}
}


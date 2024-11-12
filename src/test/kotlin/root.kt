package moe.nea.firmament.test

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import moe.nea.firmament.util.TimeMark

object FirmTestBootstrap {
	val loadStart = TimeMark.now()

	init {
		println("Bootstrap started at $loadStart")
	}

	init {
		SharedConstants.createGameVersion()
		Bootstrap.initialize()
	}

	val loadEnd = TimeMark.now()

	val loadDuration = loadStart.passedAt(loadEnd)

	init {
		println("Bootstrap completed at $loadEnd after $loadDuration")
	}

	fun bootstrapMinecraft() {
	}
}

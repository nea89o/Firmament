package moe.nea.firmament.test.testutil

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import moe.nea.firmament.test.FirmTestBootstrap

class KotestPlugin : AbstractProjectConfig() {
	override fun extensions(): List<Extension> {
		return listOf()
	}

	override suspend fun beforeProject() {
		FirmTestBootstrap.bootstrapMinecraft()
		super.beforeProject()
	}
}

package moe.nea.firmament.features.texturepack

import moe.nea.firmament.util.compatloader.CompatMeta
import moe.nea.firmament.util.compatloader.ICompatMeta

@CompatMeta
object Compat : ICompatMeta {
	override fun shouldLoad(): Boolean {
		return true
	}
}

package moe.nea.firmament.features.fixes

import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object Fixes : FirmamentFeature {
    override val identifier: String
        get() = "fixes"

    object TConfig : ManagedConfig(identifier) {
        val fixUnsignedPlayerSkins by toggle("player-skins") { true }
    }

    override val config: ManagedConfig
        get() = TConfig

    override fun onLoad() {
    }
}

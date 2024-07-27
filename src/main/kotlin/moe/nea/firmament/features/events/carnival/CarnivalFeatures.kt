/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.events.carnival

import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object CarnivalFeatures : FirmamentFeature {
    object TConfig : ManagedConfig(identifier) {
        val enableBombSolver by toggle("bombs-solver") { true }
        val displayTutorials by toggle("tutorials") { true }
    }

    override val config: ManagedConfig?
        get() = TConfig
    override val identifier: String
        get() = "carnival"
}

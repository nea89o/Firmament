/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.diana

import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.SoundReceiveEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object DianaWaypoints : FirmamentFeature {
    override val identifier: String
        get() = "diana-waypoints"
    override val config: ManagedConfig?
        get() = TConfig

    object TConfig : ManagedConfig(identifier) {
        val ancestralSpadeSolver by toggle("ancestral-spade") { false }
    }

    override fun onLoad() {
        ParticleSpawnEvent.subscribe(AncestralSpadeSolver::onParticleSpawn)
        SoundReceiveEvent.subscribe(AncestralSpadeSolver::onPlaySound)
        WorldRenderLastEvent.subscribe(AncestralSpadeSolver::onWorldRender)
    }
}



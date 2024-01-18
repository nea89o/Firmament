/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.diana

import moe.nea.firmament.events.AttackBlockEvent
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.SoundReceiveEvent
import moe.nea.firmament.events.UseBlockEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object DianaWaypoints : FirmamentFeature {
    override val identifier get() = "diana"
    override val config get() = TConfig

    object TConfig : ManagedConfig(identifier) {
        val ancestralSpadeSolver by toggle("ancestral-spade") { true }
        val nearbyWaypoints by toggle("nearby-waypoints") { true }
    }

    override fun onLoad() {
        ParticleSpawnEvent.subscribe(NearbyBurrowsSolver::onParticles)
        WorldReadyEvent.subscribe(NearbyBurrowsSolver::onSwapWorld)
        WorldRenderLastEvent.subscribe(NearbyBurrowsSolver::onRender)
        UseBlockEvent.subscribe {
            NearbyBurrowsSolver.onBlockClick(it.hitResult.blockPos)
        }
        AttackBlockEvent.subscribe {
            NearbyBurrowsSolver.onBlockClick(it.blockPos)
        }
        ProcessChatEvent.subscribe(NearbyBurrowsSolver::onChatEvent)


        ParticleSpawnEvent.subscribe(AncestralSpadeSolver::onParticleSpawn)
        SoundReceiveEvent.subscribe(AncestralSpadeSolver::onPlaySound)
        WorldRenderLastEvent.subscribe(AncestralSpadeSolver::onWorldRender)
        WorldReadyEvent.subscribe(AncestralSpadeSolver::onSwapWorld)
    }
}



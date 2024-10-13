package moe.nea.firmament.features.diana

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.AttackBlockEvent
import moe.nea.firmament.events.UseBlockEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object DianaWaypoints : FirmamentFeature {
    override val identifier get() = "diana"
    override val config get() = TConfig

    object TConfig : ManagedConfig(identifier, Category.EVENTS) {
        val ancestralSpadeSolver by toggle("ancestral-spade") { true }
        val ancestralSpadeTeleport by keyBindingWithDefaultUnbound("ancestral-teleport")
        val nearbyWaypoints by toggle("nearby-waypoints") { true }
    }


    @Subscribe
    fun onBlockUse(event: UseBlockEvent) {
        NearbyBurrowsSolver.onBlockClick(event.hitResult.blockPos)
    }

    @Subscribe
    fun onBlockAttack(event: AttackBlockEvent) {
        NearbyBurrowsSolver.onBlockClick(event.blockPos)
    }
}



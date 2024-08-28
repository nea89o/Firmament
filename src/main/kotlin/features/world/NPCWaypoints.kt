package moe.nea.firmament.features.world

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ReloadRegistrationEvent
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil

object NPCWaypoints {

    var allNpcWaypoints = listOf<NavigableWaypoint>()

    @Subscribe
    fun onRepoReloadRegistration(event: ReloadRegistrationEvent) {
        event.repo.registerReloadListener {
            allNpcWaypoints = it.items.items.values
                .asSequence()
                .filter { !it.island.isNullOrBlank() }
                .map {
                    NavigableWaypoint.NPCWaypoint(it)
                }
                .toList()
        }
    }

    @Subscribe
    fun onOpenGui(event: CommandEvent.SubCommand) {
        event.subcommand("npcs") {
            thenExecute {
                ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen(
                    "npc_waypoints",
                    NpcWaypointGui(allNpcWaypoints),
                    null))
            }
        }
    }


}

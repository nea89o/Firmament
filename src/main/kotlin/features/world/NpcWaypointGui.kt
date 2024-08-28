package moe.nea.firmament.features.world

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import moe.nea.firmament.features.events.anniversity.AnniversaryFeatures.atOnce
import moe.nea.firmament.keybindings.SavedKeyBinding

class NpcWaypointGui(
    val allWaypoints: List<NavigableWaypoint>,
) {

    data class NavigableWaypointW(val waypoint: NavigableWaypoint) {
        @Bind
        fun name() = waypoint.name

        @Bind
        fun isSelected() = NavigationHelper.targetWaypoint == waypoint

        @Bind
        fun click() {
            if (SavedKeyBinding.isShiftDown()) {
                NavigationHelper.targetWaypoint = waypoint
                NavigationHelper.tryWarpNear()
            } else if (isSelected()) {
                NavigationHelper.targetWaypoint = null
            } else {
                NavigationHelper.targetWaypoint = waypoint
            }
        }
    }

    @JvmField
    @field:Bind
    var search: String = ""
    var lastSearch: String? = null

    @Bind("results")
    fun results(): ObservableList<NavigableWaypointW> {
        return results
    }

    @Bind
    fun tick() {
        if (search != lastSearch) {
            updateSearch()
            lastSearch = search
        }
    }

    val results: ObservableList<NavigableWaypointW> = ObservableList(mutableListOf())

    fun updateSearch() {
        val split = search.split(" +".toRegex())
        results.atOnce {
            results.clear()
            allWaypoints.filter { waypoint ->
                if (search.isBlank()) {
                    true
                } else {
                    split.all { waypoint.name.contains(it, ignoreCase = true) }
                }
            }.mapTo(results) {
                NavigableWaypointW(it)
            }
        }
    }

}

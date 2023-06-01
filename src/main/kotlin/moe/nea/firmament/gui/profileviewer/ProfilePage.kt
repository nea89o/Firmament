package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.widget.WWidget

interface ProfilePage {
    fun getElements(profileViewer: ProfileViewer): WWidget
}

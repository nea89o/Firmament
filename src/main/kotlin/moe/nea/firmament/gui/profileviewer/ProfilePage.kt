package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import net.minecraft.text.Text

interface ProfilePage {
    fun getElements(profileViewer: ProfileViewer): WWidget
    val icon: Icon
    val text: Text
}

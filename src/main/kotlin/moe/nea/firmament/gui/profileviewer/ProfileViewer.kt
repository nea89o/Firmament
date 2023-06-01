package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import java.util.UUID
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.Profile

class ProfileViewer(
    val primaryPlayer: UUID,
    val playerNames: Map<UUID, String>,
    val profile: Profile,
) : LightweightGuiDescription() {

    val member: Member = profile.members[primaryPlayer] ?: error("Primary player not in profile")
    val primaryName: String = playerNames[primaryPlayer] ?: error("Primary player has no name")

    init {
        val panel = WTabPanel().also { rootPanel = it }
        panel.backgroundPainter
        listOf<ProfilePage>(SkillPage)
            .forEach { page ->
                panel.add(page.getElements(this)) {
                    it.icon(page.icon)
                    it.tooltip(page.text)
                }
            }
    }
}



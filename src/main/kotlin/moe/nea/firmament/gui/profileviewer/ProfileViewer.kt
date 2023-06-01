package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import java.util.UUID
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.gui.WBar
import moe.nea.firmament.util.toShedaniel

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
        panel.add(SkillPage.getElements(this)) {
            it.icon(ItemIcon(Items.IRON_SWORD))
            it.title(Text.translatable("firmament.pv.skills"))
        }
    }
}



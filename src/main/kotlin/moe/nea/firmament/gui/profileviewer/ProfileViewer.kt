package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import java.util.UUID
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.repo.RepoManager

class ProfileViewer(
    val primaryPlayer: UUID,
    val playerNames: Map<UUID, String>,
    val profile: Profile,
) : LightweightGuiDescription() {
    init {
        val primaryMember = profile.members[primaryPlayer] ?: error("Primary player not in profile")
        val panel = WTabPanel().also { rootPanel = it }
        panel.backgroundPainter
        panel.add(WGridPanel().also {
            it.insets = Insets.ROOT_PANEL
            it.add(WText(Text.literal(playerNames[primaryPlayer] ?: error("Primary player has no name"))), 0, 0, 6, 1)
            for ((i, skill) in Skill.values().withIndex()) {
                val leveling = RepoManager.neuRepo.constants.leveling
                val exp = skill.accessor.get(primaryMember)
                val maxLevel = skill.getMaximumLevel(leveling)
                val level = skill.getLadder(leveling)
                    .runningFold(0.0) { a, b -> a + b }
                    .filter { it <= exp }.size
                    .coerceAtMost(maxLevel)
                it.add(WText(Text.translatable("firmament.pv.skills.${skill.name.lowercase()}")), 0, i + 1, 5, 1)
                it.add(object : WText(Text.literal("$level/$maxLevel")) {
                    override fun addTooltip(tooltip: TooltipBuilder) {
                        tooltip.add(Text.translatable("firmament.pv.skills.total", exp))
                    }
                }, 5, i + 1, 2, 1)
            }
        }) {
            it.icon(ItemIcon(Items.IRON_SWORD))
            it.title(Text.translatable("firmament.pv.skills"))
        }
    }
}

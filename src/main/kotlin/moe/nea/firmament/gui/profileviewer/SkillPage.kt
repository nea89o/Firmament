package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.text.Text
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.gui.WBar
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.toShedaniel

object SkillPage : ProfilePage {
    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WGridPanel().also {
            it.insets = Insets.ROOT_PANEL
            it.add(WText(Text.literal(profileViewer.primaryName /* with rank? */)), 0, 0, 6, 1)
            for ((i, skill) in Skill.values().withIndex()) {
                val leveling = RepoManager.neuRepo.constants.leveling
                val exp = skill.accessor.get(profileViewer.member)
                val maxLevel = skill.getMaximumLevel(leveling)
                val level = skill.getLadder(leveling)
                    .runningFold(0.0) { a, b -> a + b }
                    .filter { it <= exp }.size
                    .coerceAtMost(maxLevel)
                it.add(WText(Text.translatable("firmament.pv.skills.${skill.name.lowercase()}")), 0, i + 1, 4, 1)
                it.add(object : WBar(
                    level.toDouble(), maxLevel.toDouble(),
                    skill.color.toShedaniel(), skill.color.toShedaniel().darker(2.0)
                ) {
                    override fun addTooltip(tooltip: TooltipBuilder) {
                        tooltip.add(Text.literal("$level/$maxLevel"))
                        tooltip.add(Text.translatable("firmament.pv.skills.total", exp))
                    }
                }, 4, i + 1, 4, 1)
            }
        }
    }
}

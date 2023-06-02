package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.gui.WBar
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.toShedaniel

object SkillPage : ProfilePage {

    private fun skillBar(profileViewer: ProfileViewer, skill: Skill): WBar {
        val leveling = RepoManager.neuRepo.constants.leveling
        val exp = skill.accessor.get(profileViewer.member)
        val maxLevel = skill.getMaximumLevel(leveling)
        val level = skill.getLadder(leveling)
            .runningFold(0.0) { a, b -> a + b }
            .filter { it <= exp }.size
            .coerceAtMost(maxLevel)
        return object : WBar(
            level.toDouble(), maxLevel.toDouble(),
            skill.color.toShedaniel(), skill.color.toShedaniel().darker(2.0)
        ) {
            override fun addTooltip(tooltip: TooltipBuilder) {
                tooltip.add(Text.literal("$level/$maxLevel"))
                tooltip.add(Text.translatable("firmament.pv.skills.total", FirmFormatters.toString(exp, 1)))
            }
        }
    }

    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WGridPanel().also {
            it.insets = Insets.ROOT_PANEL
            it.add(WText(Text.literal(profileViewer.account.getDisplayName())), 0, 0, 6, 1)
            for ((i, skill) in Skill.values().withIndex()) {
                it.add(WText(Text.translatable("firmament.pv.skills.${skill.name.lowercase()}")), 0, i + 1, 4, 1)
                it.add(skillBar(profileViewer, skill), 4, i + 1, 4, 1)
            }
        }
    }

    override val icon: Icon
        get() = ItemIcon(ItemStack(Items.IRON_SWORD))
    override val text: Text
        get() = Text.translatable("firmament.pv.skills")
}

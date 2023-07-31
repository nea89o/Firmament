/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import moe.nea.firmament.apis.CollectionInfo
import moe.nea.firmament.apis.CollectionType
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.gui.WBar
import moe.nea.firmament.gui.WFixedPanel
import moe.nea.firmament.gui.WTitledItem
import moe.nea.firmament.hud.horizontal
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.modifyLore
import moe.nea.firmament.util.toShedaniel
import moe.nea.firmament.util.toTextColor

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

    private fun collectionItem(type: CollectionType, info: CollectionInfo, color: DyeColor, member: Member): WWidget {
        val collectionCount = member.collection[type] ?: 0
        val unlockedTiers = info.tiers.count { it.amountRequired <= collectionCount }
        return WTitledItem(
            SBItemEntryDefinition.getEntry(type.skyblockId).asItemEntry().value.copy()
                .also {
                    it.setCustomName(
                        Text.literal(info.name).fillStyle(
                            Style.EMPTY.withItalic(false).withBold(true)
                                .withColor(color.toTextColor())
                        )
                    )
                    it.modifyLore { old ->
                        listOf(
                            Text.literal("${info.name} Collection: $collectionCount / ${info.tiers.last().amountRequired}"),
                            Text.literal("Tiers unlocked: $unlockedTiers")
                        ).map {
                            it.fillStyle(
                                Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)
                            )
                        }
                    }
                }, countString = Text.literal("$unlockedTiers").styled {
                if (unlockedTiers == info.maxTiers)
                    it.withColor(Formatting.YELLOW)
                else it
            }
        )
    }

    private fun collectionPanel(profileViewer: ProfileViewer): WTabPanel {
        return WTabPanel().also {
            val data = HypixelStaticData.collectionData
            val panels = mutableListOf<WPanel>()
            for ((skill, collections) in data.entries) {
                val skillT = Skill.values().find { it.name == skill }
                val color = skillT?.color ?: DyeColor.BLACK
                val icon = skillT?.icon?.let { RepoManager.getNEUItem(it).asItemStack() } ?: ItemStack(Items.ITEM_FRAME)
                val panel = WBox(Axis.HORIZONTAL).also {
                    it.horizontalAlignment = HorizontalAlignment.CENTER
                    it.add(WFixedPanel(WGridPanel().also {
                        it.insets = Insets.ROOT_PANEL
                        it.setGaps(2, 2)
                        var x = 0
                        var y = 0
                        for (item in collections.items) {
                            it.add(collectionItem(item.key, item.value, color, profileViewer.member), x, y, 1, 1)
                            x++
                            if (x == 5) {
                                x = 0
                                y++
                            }
                        }
                    }))
                }
                panels.add(panel)
                it.add(panel) {
                    it.tooltip(
                        Text.translatable("firmament.pv.skills.${skill.lowercase()}")
                            .styled { it.withColor(color.toTextColor()) })
                    it.icon(ItemIcon(icon))
                }
            }
            it.layout()
            val tabWidth = it.width
            panels.forEach { it.setSize(tabWidth - Insets.ROOT_PANEL.horizontal, it.height) }
        }
    }

    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WBox(Axis.HORIZONTAL).also {
            it.insets = Insets.ROOT_PANEL
            it.add(WGridPanel().also {
                it.add(WText(Text.literal(profileViewer.account.getDisplayName(profileViewer.primaryName))), 0, 0, 8, 1)
                for ((i, skill) in Skill.values().withIndex()) {
                    it.add(WText(Text.translatable("firmament.pv.skills.${skill.name.lowercase()}")), 0, i + 1, 4, 1)
                    it.add(skillBar(profileViewer, skill), 4, i + 1, 4, 1)
                }
            })
            it.add(collectionPanel(profileViewer))
        }
    }

    override val icon: Icon
        get() = ItemIcon(ItemStack(Items.IRON_SWORD))
    override val text: Text
        get() = Text.translatable("firmament.pv.skills")
}

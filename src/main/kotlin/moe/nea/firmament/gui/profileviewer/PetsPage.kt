package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import io.github.moulberry.repo.data.Rarity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.gui.WTightScrollPanel
import moe.nea.firmament.gui.WTitledItem
import moe.nea.firmament.rei.PetData
import moe.nea.firmament.rei.SBItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters

object PetsPage : ProfilePage {
    private fun petOverview(profileViewer: ProfileViewer, choosePet: (ItemStack) -> Unit) = WGridPanel().also { panel ->
        panel.insets = Insets.ROOT_PANEL
        panel.add(WText(Text.literal(profileViewer.account.getDisplayName(profileViewer.primaryName))), 0, 0, 6, 1)
        panel.add((WTightScrollPanel(WGridPanel().also { it ->
            it.setGaps(8, 8)

            for ((i, pet) in profileViewer.member.pets.map {
                SBItemStack(it.itemId, PetData(it.tier, it.type.name, it.exp))
            }.sortedWith(
                Comparator.comparing<SBItemStack?, Rarity?> { it.petData!!.rarity }.reversed()
                    .thenDescending(Comparator.comparing { it.petData!!.levelData.currentLevel })
                    .thenDescending(Comparator.comparing { it.petData!!.petId })
            ).withIndex()) {
                val stack = pet.asItemStack()
                it.add(object : WTitledItem(stack) {
                    override fun onClick(x: Int, y: Int, button: Int): InputResult {
                        choosePet(stack)
                        return InputResult.PROCESSED
                    }
                }, i % 9, i / 9, 1, 1)
            }
            it.layout()
        })), 0, 1, 12, 8)
        petStats(profileViewer).withIndex().forEach { (i, it) ->
            panel.add(it, 0, 10 + i, 8, 1)
        }
    }

    private fun petStats(profileViewer: ProfileViewer): List<WWidget> {
        val petScore = profileViewer.member.pets.groupBy { it.type }
            .map { it.value.maxBy { it.tier } }
            .sumOf { RepoManager.neuRepo.constants.bonuses.getPetValue(it.tier) }

        return listOf(
            WText(
                Text.literal("Pet Score: ").styled { it.withColor(Formatting.AQUA) }
                    .append(Text.literal("$petScore").styled { it.withColor(Formatting.GOLD) })
            ),
            WText(
                Text.literal("Magic Find: ").styled { it.withColor(Formatting.AQUA) }
                    .append(
                        Text.literal(
                            FirmFormatters.toString(
                                RepoManager.neuRepo.constants.bonuses.getPetRewards(
                                    petScore
                                )["magic_find"] ?: 0.0F, 1
                            )
                        )
                            .styled { it.withColor(Formatting.GOLD) })
            )
        )
    }

    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WBox(Axis.HORIZONTAL).also {
            it.insets = Insets.ROOT_PANEL
            val item = WTitledItem(ItemStack.EMPTY)
            item.backgroundPainter = BackgroundPainter.VANILLA
            it.add(WBox(Axis.VERTICAL).also { box ->
                box.add(petOverview(profileViewer) { item.stack = it })
            })
            val b = WBox(Axis.VERTICAL).also { box ->
                box.verticalAlignment = VerticalAlignment.CENTER
                box.horizontalAlignment = HorizontalAlignment.CENTER
                box.add(item, 128, 128)
            }
            it.add(b)
            it.layout()
            b.setSize(b.width + 20, it.height)
        }
    }

    override val icon: Icon
        get() = ItemIcon(Items.BONE)
    override val text: Text
        get() = Text.translatable("firmament.pv.pets")
}

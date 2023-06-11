package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItem
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.gui.WTightScrollPanel
import moe.nea.firmament.gui.WTitledItem
import moe.nea.firmament.rei.PetData
import moe.nea.firmament.rei.SBItemStack
import moe.nea.firmament.repo.RepoManager

object PetsPage : ProfilePage {
    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WGridPanel().also {
            it.insets = Insets.ROOT_PANEL
            it.add(WText(Text.literal(profileViewer.account.getDisplayName())), 0, 0, 6, 1)
            it.add((WTightScrollPanel(WGridPanel().also {
                it.setGaps(8, 8)
                for ((i, pet) in profileViewer.member.pets.withIndex()) {
                    val stack = SBItemStack(pet.itemId, PetData(pet.tier, pet.type.name, pet.exp)).asItemStack()
                    it.add(WTitledItem(stack), i % 5, i / 5, 1, 1)
                }
                it.layout()
            })), 0, 1, 8, 8)
        }
    }

    override val icon: Icon
        get() = ItemIcon(Items.BONE)
    override val text: Text
        get() = Text.translatable("firmament.pv.skills")
}

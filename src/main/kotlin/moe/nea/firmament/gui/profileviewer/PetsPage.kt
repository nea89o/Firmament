package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItem
import io.github.cottonmc.cotton.gui.widget.WScrollPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.gui.WFixedPanel
import moe.nea.firmament.gui.WTightScrollPanel
import moe.nea.firmament.rei.SBItemStack

object PetsPage : ProfilePage {
    override fun getElements(profileViewer: ProfileViewer): WWidget {
        return WGridPanel().also {
            it.insets = Insets.ROOT_PANEL
            it.add(WText(Text.literal(profileViewer.account.getDisplayName())), 0, 0, 6, 1)
            it.add((WTightScrollPanel(WGridPanel().also {
                it.setGaps(8, 8)
                for ((i, pet) in profileViewer.member.pets.withIndex()) {
                    val stack = SBItemStack(pet.itemId, 1).asItemStack()
                    it.add(object : WItem(stack) {
                        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                            BackgroundPainter.SLOT.paintBackground(matrices, x, y, this)
                            super.paint(matrices, x, y, mouseX, mouseY)
                        }

                        override fun addTooltip(tooltip: TooltipBuilder) {
                            tooltip.add(*stack.getTooltip(null, TooltipContext.BASIC).toTypedArray())
                        }
                    }, i % 5, i / 5, 1, 1)
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

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.buttons

import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId

@Serializable
data class InventoryButton(
    var x: Int,
    var y: Int,
    var anchorRight: Boolean,
    var anchorBottom: Boolean,
    var icon: String? = "",
    var command: String? = "",
) {
    companion object {
        val dimensions = Dimension(18, 18)
        fun getItemForName(icon: String): ItemStack {
            return RepoManager.getNEUItem(SkyblockId(icon)).asItemStack(idHint = SkyblockId(icon))
        }
    }

    fun render(context: DrawContext) {
        context.drawSprite(
            0,
            0,
            0,
            dimensions.width,
            dimensions.height,
            MC.guiAtlasManager.getSprite(Identifier("firmament:inventory_button_background"))
        )
        context.drawItem(getItem(), 1, 1)
    }

    fun isValid() = !icon.isNullOrBlank() && !command.isNullOrBlank()

    fun getPosition(guiRect: Rectangle): Point {
        return Point(
            (if (anchorRight) guiRect.maxX else guiRect.minX) + x,
            (if (anchorBottom) guiRect.maxY else guiRect.minY) + y,
        )
    }

    fun getBounds(guiRect: Rectangle): Rectangle {
        return Rectangle(getPosition(guiRect), dimensions)
    }

    fun getItem(): ItemStack {
        return getItemForName(icon ?: "")
    }

}

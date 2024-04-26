/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.profileviewer

import com.mojang.brigadier.StringReader
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WText
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import moe.nea.lisp.LispData
import moe.nea.lisp.LispExecutionContext
import moe.nea.lisp.LispParser
import moe.nea.lisp.bind.AutoBinder
import moe.nea.lisp.bind.LispBinding
import moe.nea.lisp.bind.UnmapForeignObject
import net.minecraft.command.argument.ItemStringReader
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.gui.WTitledItem
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.item.setCustomName
import moe.nea.firmament.util.modifyLore

class ProfileViewerLibrary {

    @LispBinding("mk-item")
    fun makeItem(itemType: String, title: String, vararg lore: String): LispData.ForeignObject<ItemStack> {
        val item = ItemStringReader(MC.defaultRegistries).consume(StringReader(itemType))
        val itemStack = ItemStack(item.item.value())
        itemStack.applyComponentsFrom(item.components)
        itemStack.modifyLore { lore.map { Text.literal(it) } }
        itemStack.setCustomName(Text.literal(title))
        return LispData.ForeignObject(itemStack)
    }

    @LispBinding("def-page")
    fun defPage(name: String, @UnmapForeignObject icon: ItemStack) {
        pages.add(Pair(name, icon))
    }

    val pages = mutableListOf<Pair<String, ItemStack>>()
    val coreEnvironment = LispExecutionContext()

    fun run() {
        val t = coreEnvironment.genBindings()
        val ab = AutoBinder()
        ab.bindTo(this, t)
        val prog = LispParser.parse(
            "testfile.lisp", """
        (def-page "Test" (mk-item "minecraft:tnt" "§aThis is a test page" "§aMore text"))
        (def-page "Skills" (mk-item "minecraft:diamond_sword" "§aThis is a test page" "§aMore text"))
        """.trimIndent()
        )
        coreEnvironment.executeProgram(t, prog)
        val light = LightweightGuiDescription()
        val root = light.rootPanel as WGridPanel
        root.setGaps(8, 8)
        pages.forEachIndexed { i, (name, item) ->
            root.add(WTitledItem(item), 0, i)
            root.add(WText(Text.literal(name)).also { it.verticalAlignment = VerticalAlignment.CENTER }, 1, i, 6, 1)
        }
        ScreenUtil.setScreenLater(CottonClientScreen(light))
    }
}

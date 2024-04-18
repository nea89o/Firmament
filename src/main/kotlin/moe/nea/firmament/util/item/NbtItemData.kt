/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text

fun textFromNbt() {

}

val ItemStack.loreAccordingToNbt
    get() = getOrCreateSubNbt(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE.toInt())
        .map {
            lazy(LazyThreadSafetyMode.NONE) {
                Text.Serialization.fromJson((it as NbtString).asString())
            }
        }

val ItemStack.displayNameAccordingToNbt
    get() = getOrCreateSubNbt(ItemStack.DISPLAY_KEY).let {
        if (it.contains(ItemStack.NAME_KEY, NbtElement.STRING_TYPE.toInt()))
            Text.Serialization.fromJson(it.getString(ItemStack.NAME_KEY))
        else
            null
    }

/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType

object RestArgumentType : ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        val remaining = reader.remaining
        reader.cursor += remaining.length
        return remaining
    }

}

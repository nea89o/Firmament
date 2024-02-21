/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.util.removeColorCodes


class ColorCode {
    @Test
    fun testWhatever() {
        Assertions.assertEquals("", "".removeColorCodes().toString())
        Assertions.assertEquals("", "§".removeColorCodes().toString())
        Assertions.assertEquals("", "§a".removeColorCodes().toString())
        Assertions.assertEquals("ab", "a§ab".removeColorCodes().toString())
        Assertions.assertEquals("ab", "a§ab§§".removeColorCodes().toString())
        Assertions.assertEquals("abc", "a§ab§§c".removeColorCodes().toString())
        Assertions.assertEquals("bc", "§ab§§c".removeColorCodes().toString())
        Assertions.assertEquals("b§lc", "§ab§l§§c".removeColorCodes(true).toString())
        Assertions.assertEquals("b§lc§l", "§ab§l§§c§l".removeColorCodes(true).toString())
        Assertions.assertEquals("§lb§lc", "§l§ab§l§§c".removeColorCodes(true).toString())
    }
}

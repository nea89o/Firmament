package moe.nea.firmament.test.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import moe.nea.firmament.util.removeColorCodes


class ColorCodeTest {
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

    @Test
    fun testEdging() {
        Assertions.assertEquals("", "§".removeColorCodes())
        Assertions.assertEquals("a", "a§".removeColorCodes())
        Assertions.assertEquals("b", "§ab§".removeColorCodes())
    }

    @Test
    fun `testDouble§`() {
        Assertions.assertEquals("1", "§§1".removeColorCodes())
    }

    @Test
    fun testKeepNonColor() {
        Assertions.assertEquals("§k§l§m§n§o§r", "§k§l§m§f§n§o§r".removeColorCodes(true))
    }

    @Test
    fun testPlainString() {
        Assertions.assertEquals("bcdefgp", "bcdefgp".removeColorCodes())
        Assertions.assertEquals("", "".removeColorCodes())
    }

    @Test
    fun testSomeNormalTestCases() {
        Assertions.assertEquals(
            "You are not currently in a party.",
            "§r§cYou are not currently in a party.§r".removeColorCodes()
        )
        Assertions.assertEquals(
            "Ancient Necron's Chestplate ✪✪✪✪",
            "§dAncient Necron's Chestplate §6✪§6✪§6✪§6✪".removeColorCodes()
        )
    }
}

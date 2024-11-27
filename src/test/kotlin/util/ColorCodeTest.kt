package moe.nea.firmament.test.util

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions
import moe.nea.firmament.util.removeColorCodes


class ColorCodeTest : AnnotationSpec() {
    @Test
    fun testWhatever() {
        Assertions.assertEquals("", "".removeColorCodes())
        Assertions.assertEquals("", "§".removeColorCodes())
        Assertions.assertEquals("", "§a".removeColorCodes())
        Assertions.assertEquals("ab", "a§ab".removeColorCodes())
        Assertions.assertEquals("ab", "a§ab§§".removeColorCodes())
        Assertions.assertEquals("abc", "a§ab§§c".removeColorCodes())
        Assertions.assertEquals("bc", "§ab§§c".removeColorCodes())
        Assertions.assertEquals("b§lc", "§ab§l§§c".removeColorCodes(true))
        Assertions.assertEquals("b§lc§l", "§ab§l§§c§l".removeColorCodes(true))
        Assertions.assertEquals("§lb§lc", "§l§ab§l§§c".removeColorCodes(true))
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

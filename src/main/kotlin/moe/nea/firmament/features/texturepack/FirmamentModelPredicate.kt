
package moe.nea.firmament.features.texturepack

import net.minecraft.item.ItemStack

interface FirmamentModelPredicate {
    fun test(stack: ItemStack): Boolean
}

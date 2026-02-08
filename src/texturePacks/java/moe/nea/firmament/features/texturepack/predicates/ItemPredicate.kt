
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import moe.nea.firmament.util.MC

class ItemPredicate(
    val item: Item
) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return stack.`is`(item)
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): ItemPredicate? {
            if (jsonElement is JsonPrimitive && jsonElement.isString) {
                val itemKey = ResourceKey.create(Registries.ITEM,
                                             Identifier.tryParse(jsonElement.asString)
                                                 ?: return null)
                return ItemPredicate(MC.defaultItems.get(itemKey).getOrNull()?.value() ?: return null)
            }
            return null
        }
    }
}

package moe.nea.firmament.events

import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedModelManager
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.collections.WeakCache

data class CustomItemModelEvent(
    val itemStack: ItemStack,
    var overrideModel: ModelIdentifier? = null,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<CustomItemModelEvent>() {
        val cache =
            WeakCache.memoize<ItemStack, BakedModelManager, Optional<BakedModel>>("CustomItemModels") { stack, models ->
                val modelId = getModelIdentifier(stack) ?: return@memoize Optional.empty()
                val bakedModel = models.getModel(modelId)
                if (bakedModel === models.missingModel) return@memoize Optional.empty()
                Optional.of(bakedModel)
            }

        @JvmStatic
        fun getModelIdentifier(itemStack: ItemStack?): ModelIdentifier? {
            if (itemStack == null) return null
            return publish(CustomItemModelEvent(itemStack)).overrideModel
        }

        @JvmStatic
        fun getModel(itemStack: ItemStack?, thing: BakedModelManager): BakedModel? {
            if (itemStack == null) return null
            return cache.invoke(itemStack, thing).getOrNull()
        }
    }
}

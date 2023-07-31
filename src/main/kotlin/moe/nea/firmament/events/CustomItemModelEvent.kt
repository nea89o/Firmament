/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import java.util.*
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedModelManager
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.ItemStack

data class CustomItemModelEvent(
    val itemStack: ItemStack,
    var overrideModel: ModelIdentifier? = null,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<CustomItemModelEvent>() {
        private val cache = IdentityHashMap<ItemStack?, Any>()
        private val sentinelNull = Object()

        fun clearCache() {
            cache.clear()
        }

        @JvmStatic
        fun getModelIdentifier(itemStack: ItemStack?): ModelIdentifier? {
            if (itemStack == null) return null
            return publish(CustomItemModelEvent(itemStack)).overrideModel
        }

        @JvmStatic
        fun getModel(itemStack: ItemStack?, thing: BakedModelManager): BakedModel? {
            if (itemStack == null) return null
            val cachedValue = cache.getOrPut(itemStack) {
                val modelId = getModelIdentifier(itemStack) ?: return@getOrPut sentinelNull
                val bakedModel = thing.getModel(modelId)
                if (bakedModel === thing.missingModel) return@getOrPut sentinelNull
                bakedModel
            }
            if (cachedValue === sentinelNull)
                return null
            return cachedValue as BakedModel
        }
    }
}

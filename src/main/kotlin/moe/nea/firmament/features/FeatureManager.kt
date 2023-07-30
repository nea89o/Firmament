/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.chat.ImagePreview
import moe.nea.firmament.features.debug.DebugView
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.features.fishing.FishingWarning
import moe.nea.firmament.features.fixes.Fixes
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.features.inventory.SaveCursorPosition
import moe.nea.firmament.features.inventory.SlotLocking
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlay
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.util.data.DataHolder

object FeatureManager : DataHolder<FeatureManager.Config>(serializer(), "features", ::Config) {
    @Serializable
    data class Config(
        val enabledFeatures: MutableMap<String, Boolean> = mutableMapOf()
    )

    private val features = mutableMapOf<String, FirmamentFeature>()

    val allFeatures: Collection<FirmamentFeature> get() = features.values

    private var hasAutoloaded = false

    init {
        autoload()
    }

    fun autoload() {
        synchronized(this) {
            if (hasAutoloaded) return
            loadFeature(FairySouls)
            loadFeature(FishingWarning)
            loadFeature(SlotLocking)
            loadFeature(StorageOverlay)
            loadFeature(CraftingOverlay)
            loadFeature(ImagePreview)
            loadFeature(SaveCursorPosition)
            loadFeature(CustomSkyBlockTextures)
            loadFeature(Fixes)
            if (Firmament.DEBUG) {
                loadFeature(DeveloperFeatures)
                loadFeature(DebugView)
            }
            hasAutoloaded = true
        }
    }

    fun loadFeature(feature: FirmamentFeature) {
        synchronized(features) {
            if (feature.identifier in features) {
                Firmament.logger.error("Double registering feature ${feature.identifier}. Ignoring second instance $feature")
                return
            }
            features[feature.identifier] = feature
            feature.onLoad()
        }
    }

    fun isEnabled(identifier: String): Boolean? =
        data.enabledFeatures[identifier]


    fun setEnabled(identifier: String, value: Boolean) {
        data.enabledFeatures[identifier] = value
        markDirty()
    }

}

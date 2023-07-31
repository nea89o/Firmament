/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.apache.logging.log4j.LogManager
import org.lwjgl.glfw.GLFW
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes
import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.CollectionResponse
import moe.nea.firmament.apis.CollectionSkillData
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.keybindings.IKeyBinding
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.async.waitForInput

object HypixelStaticData {
    private val logger = LogManager.getLogger("Firmament.HypixelStaticData")
    private val moulberryBaseUrl = "https://moulberry.codes"
    private val hypixelApiBaseUrl = "https://api.hypixel.net"
    var lowestBin: Map<SkyblockId, Double> = mapOf()
        private set
    var bazaarData: Map<SkyblockId, BazaarData> = mapOf()
        private set
    var collectionData: Map<String, CollectionSkillData> = mapOf()
        private set

    @Serializable
    data class BazaarData(
        @SerialName("product_id")
        val productId: SkyblockId.BazaarStock,
        @SerialName("quick_status")
        val quickStatus: BazaarStatus,
    )

    @Serializable
    data class BazaarStatus(
        val sellPrice: Double,
        val sellVolume: Long,
        val sellMovingWeek: Long,
        val sellOrders: Long,
        val buyPrice: Double,
        val buyVolume: Long,
        val buyMovingWeek: Long,
        val buyOrders: Long
    )

    @Serializable
    private data class BazaarResponse(
        val success: Boolean,
        val products: Map<SkyblockId.BazaarStock, BazaarData> = mapOf(),
    )

    fun getPriceOfItem(item: SkyblockId): Double? = bazaarData[item]?.quickStatus?.buyPrice ?: lowestBin[item]


    fun spawnDataCollectionLoop() {
        Firmament.coroutineScope.launch {
            logger.info("Updating collection data")
            updateCollectionData()
        }
        Firmament.coroutineScope.launch {
            while (true) {
                logger.info("Updating NEU prices")
                updatePrices()
                withTimeoutOrNull(10.minutes) { waitForInput(IKeyBinding.ofKeyCode(GLFW.GLFW_KEY_U)) }
            }
        }
    }

    private suspend fun updatePrices() {
        awaitAll(
            Firmament.coroutineScope.async { fetchBazaarPrices() },
            Firmament.coroutineScope.async { fetchPricesFromMoulberry() },
        )
    }

    private suspend fun fetchPricesFromMoulberry() {
        lowestBin = Firmament.httpClient.get("$moulberryBaseUrl/lowestbin.json")
            .body<Map<SkyblockId, Double>>()
    }

    private suspend fun fetchBazaarPrices() {
        val response = Firmament.httpClient.get("$hypixelApiBaseUrl/skyblock/bazaar").body<BazaarResponse>()
        if (!response.success) {
            logger.warn("Retrieved unsuccessful bazaar data")
        }
        bazaarData = response.products.mapKeys { it.key.toRepoId() }
    }

    private suspend fun updateCollectionData() {
        val response =
            Firmament.httpClient.get("$hypixelApiBaseUrl/resources/skyblock/collections").body<CollectionResponse>()
        if (!response.success) {
            logger.warn("Retrieved unsuccessful collection data")
        }
        collectionData = response.collections
        logger.info("Downloaded ${collectionData.values.sumOf { it.items.values.size }} collections")
    }

}

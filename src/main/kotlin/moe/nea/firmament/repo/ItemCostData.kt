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
import moe.nea.firmament.keybindings.IKeyBinding
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.async.waitForInput

object ItemCostData {
    private val logger = LogManager.getLogger("Firmament.ItemCostData")
    private val moulberryBaseUrl = "https://moulberry.codes"
    private val hypixelApiBaseUrl = "https://api.hypixel.net"
    var lowestBin: Map<SkyblockId, Double> = mapOf()
        private set
    var bazaarData: Map<SkyblockId, BazaarData> = mapOf()
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

    fun spawnPriceLoop() {
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

}

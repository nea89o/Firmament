package moe.nea.firmament.repo

import org.apache.logging.log4j.LogManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes
import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.CollectionResponse
import moe.nea.firmament.apis.CollectionSkillData
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.net.HttpUtil

object HypixelStaticData {
	private val logger = LogManager.getLogger("Firmament.HypixelStaticData")
	private val hypixelApiBaseUrl = "https://api.hypixel.net"
	var lowestBin: Map<SkyblockId, Double> = mapOf()
		private set
	var avg1dlowestBin: Map<SkyblockId, Double> = mapOf()
		private set
	var avg3dlowestBin: Map<SkyblockId, Double> = mapOf()
		private set
	var avg7dlowestBin: Map<SkyblockId, Double> = mapOf()
		private set
	var bazaarData: Map<SkyblockId.BazaarStock, BazaarData> = mapOf()
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


	fun getPriceOfItem(item: SkyblockId): Double? =
		bazaarData[SkyblockId.BazaarStock.fromSkyBlockId(item)]?.quickStatus?.buyPrice ?: lowestBin[item]

	fun hasBazaarStock(item: SkyblockId.BazaarStock): Boolean {
		return item in bazaarData
	}

	fun hasAuctionHouseOffers(item: SkyblockId): Boolean {
		return (item in lowestBin) // TODO: || (item in biddableAuctionPrices)
	}

	fun spawnDataCollectionLoop() {
		Firmament.coroutineScope.launch {
			logger.info("Updating collection data")
			updateCollectionData()
		}
		Firmament.coroutineScope.launch {
			while (true) {
				logger.info("Updating auction house prices")
				fetchAuctionHousePrices()
				delay(
					2.minutes)
			}
		}
		Firmament.coroutineScope.launch {
			while (true) {
				logger.info("Updating bazaar prices")
				fetchBazaarPrices()
				delay(2.minutes)
			}
		}
	}

	private suspend fun fetchAuctionHousePrices() {
		lowestBin = HttpUtil.request("https://api.eliteskyblock.com/resources/auctions/neu")
			.forJson<Map<SkyblockId, Double>>().await()
		/*
		avg1dlowestBin = HttpUtil.request("$moulberryBaseUrl/auction_averages_lbin/1day.json")
			.forJson<Map<SkyblockId, Double>>().await()
		avg3dlowestBin = HttpUtil.request("$moulberryBaseUrl/auction_averages_lbin/3day.json")
			.forJson<Map<SkyblockId, Double>>().await()
		avg7dlowestBin = HttpUtil.request("$moulberryBaseUrl/auction_averages_lbin/7day.json")
			.forJson<Map<SkyblockId, Double>>().await()
		 */
	}

	private suspend fun fetchBazaarPrices() {
		val response = HttpUtil.request("$hypixelApiBaseUrl/skyblock/bazaar").forJson<BazaarResponse>()
			.await()
		if (!response.success) {
			logger.warn("Retrieved unsuccessful bazaar data")
		}
		bazaarData = response.products
	}

	private suspend fun updateCollectionData() {
		val response =
			HttpUtil.request("$hypixelApiBaseUrl/resources/skyblock/collections").forJson<CollectionResponse>()
				.await()
		if (!response.success) {
			logger.warn("Retrieved unsuccessful collection data")
		}
		collectionData = response.collections
		logger.info("Downloaded ${collectionData.values.sumOf { it.items.values.size }} collections")
	}

}

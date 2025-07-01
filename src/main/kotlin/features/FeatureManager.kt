package moe.nea.firmament.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.FeaturesInitializedEvent
import moe.nea.firmament.events.FirmamentEvent
import moe.nea.firmament.events.subscription.Subscription
import moe.nea.firmament.events.subscription.SubscriptionList
import moe.nea.firmament.features.chat.AutoCompletions
import moe.nea.firmament.features.chat.ChatLinks
import moe.nea.firmament.features.chat.QuickCommands
import moe.nea.firmament.features.debug.DebugView
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.features.debug.MinorTrolling
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.features.diana.DianaWaypoints
import moe.nea.firmament.features.events.anniversity.AnniversaryFeatures
import moe.nea.firmament.features.events.carnival.CarnivalFeatures
import moe.nea.firmament.features.fixes.CompatibliltyFeatures
import moe.nea.firmament.features.fixes.Fixes
import moe.nea.firmament.features.inventory.CraftingOverlay
import moe.nea.firmament.features.inventory.ItemRarityCosmetics
import moe.nea.firmament.features.inventory.PetFeatures
import moe.nea.firmament.features.inventory.PriceData
import moe.nea.firmament.features.inventory.SaveCursorPosition
import moe.nea.firmament.features.inventory.SlotLocking
import moe.nea.firmament.features.inventory.WardrobeKeybinds
import moe.nea.firmament.features.inventory.buttons.InventoryButtons
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlay
import moe.nea.firmament.features.items.EtherwarpOverlay
import moe.nea.firmament.features.mining.PickaxeAbility
import moe.nea.firmament.features.mining.PristineProfitTracker
import moe.nea.firmament.features.misc.CustomCapes
import moe.nea.firmament.features.misc.Hud
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.features.world.Waypoints
import moe.nea.firmament.util.compatloader.ICompatMeta
import moe.nea.firmament.util.data.DataHolder

object FeatureManager : DataHolder<FeatureManager.Config>(serializer(), "features", ::Config) {
	@Serializable
	data class Config(
		val enabledFeatures: MutableMap<String, Boolean> = mutableMapOf()
	)

	private val features = mutableMapOf<String, FirmamentFeature>()

	val allFeatures: Collection<FirmamentFeature> get() = features.values

	private var hasAutoloaded = false

	fun autoload() {
		synchronized(this) {
			if (hasAutoloaded) return
			loadFeature(MinorTrolling)
			loadFeature(FairySouls)
			loadFeature(AutoCompletions)
			// TODO: loadFeature(FishingWarning)
			loadFeature(SlotLocking)
			loadFeature(StorageOverlay)
			loadFeature(PristineProfitTracker)
			loadFeature(CraftingOverlay)
			loadFeature(PowerUserTools)
			loadFeature(Waypoints)
			loadFeature(ChatLinks)
			loadFeature(CompatibliltyFeatures)
			loadFeature(AnniversaryFeatures)
			loadFeature(QuickCommands)
			loadFeature(PetFeatures)
			loadFeature(SaveCursorPosition)
			loadFeature(PriceData)
			loadFeature(Fixes)
			loadFeature(CustomCapes)
			loadFeature(Hud)
			loadFeature(EtherwarpOverlay)
			loadFeature(WardrobeKeybinds)
			loadFeature(DianaWaypoints)
			loadFeature(ItemRarityCosmetics)
			loadFeature(PickaxeAbility)
			loadFeature(CarnivalFeatures)
			if (Firmament.DEBUG) {
				loadFeature(DeveloperFeatures)
				loadFeature(DebugView)
			}
			allFeatures.forEach { it.config }
			FeaturesInitializedEvent.publish(FeaturesInitializedEvent(allFeatures.toList()))
			hasAutoloaded = true
		}
	}

	fun subscribeEvents() {
		SubscriptionList.allLists.forEach { list ->
			if (ICompatMeta.shouldLoad(list.javaClass.name))
				runCatching {
					list.provideSubscriptions {
						it.owner.javaClass.classes.forEach {
							runCatching { it.getDeclaredField("INSTANCE").get(null) }
						}
						subscribeSingleEvent(it)
					}
				}.getOrElse {
					// TODO: allow annotating source sets to specifically opt out of loading for mods, maybe automatically
					Firmament.logger.info("Ignoring events from $list, likely due to a missing compat mod.", it)
				}
		}
	}

	private fun <T : FirmamentEvent> subscribeSingleEvent(it: Subscription<T>) {
		it.eventBus.subscribe(false, "${it.owner.javaClass.simpleName}:${it.methodName}", it.invoke)
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

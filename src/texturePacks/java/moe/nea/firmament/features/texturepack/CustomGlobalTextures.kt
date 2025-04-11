@file:UseSerializers(IdentifierSerializer::class, FirmamentRootPredicateSerializer::class)

package moe.nea.firmament.features.texturepack


import java.util.concurrent.CompletableFuture
import org.slf4j.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.jvm.optionals.getOrNull
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.EarlyResourceReloadEvent
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.IdentifierSerializer
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.json.SingletonSerializableList
import moe.nea.firmament.util.runNull

object CustomGlobalTextures : SinglePreparationResourceReloader<CustomGlobalTextures.CustomGuiTextureOverride>(),
	SubscriptionOwner {
	override val delegateFeature: FirmamentFeature
		get() = CustomSkyBlockTextures

	class CustomGuiTextureOverride(
		val classes: List<ItemOverrideCollection>
	)

	@Serializable
	data class GlobalItemOverride(
		val screen: @Serializable(SingletonSerializableList::class) List<Identifier>,
		val model: Identifier,
		val predicate: FirmamentModelPredicate,
	)

	@Serializable
	data class ScreenFilter(
		val title: StringMatcher,
	)

	data class ItemOverrideCollection(
		val screenFilter: ScreenFilter,
		val overrides: List<GlobalItemOverride>,
	)

	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		MC.resourceManager.registerReloader(this)
	}

	@Subscribe
	fun onEarlyReload(event: EarlyResourceReloadEvent) {
		preparationFuture = CompletableFuture
			.supplyAsync(
				{
					prepare(event.resourceManager)
				}, event.preparationExecutor)
	}

	@Volatile
	var preparationFuture: CompletableFuture<CustomGuiTextureOverride> = CompletableFuture.completedFuture(
		CustomGuiTextureOverride(listOf()))

	override fun prepare(manager: ResourceManager?, profiler: Profiler?): CustomGuiTextureOverride {
		return preparationFuture.join()
	}

	override fun apply(prepared: CustomGuiTextureOverride, manager: ResourceManager?, profiler: Profiler?) {
		guiClassOverrides = prepared
	}

	val logger = LoggerFactory.getLogger(CustomGlobalTextures::class.java)
	fun prepare(manager: ResourceManager): CustomGuiTextureOverride {
		val overrideResources =
			manager.findResources("overrides/item") { it.namespace == "firmskyblock" && it.path.endsWith(".json") }
				.mapNotNull {
					Firmament.tryDecodeJsonFromStream<GlobalItemOverride>(it.value.inputStream).getOrElse { ex ->
						ErrorUtil.softError("Failed to load global item override at ${it.key}", ex)
						null
					}
				}

		val byGuiClass = overrideResources.flatMap { override -> override.screen.toSet().map { it to override } }
			.groupBy { it.first }
		val guiClasses = byGuiClass.entries
			.mapNotNull {
				val key = it.key
				val guiClassResource =
					manager.getResource(Identifier.of(key.namespace, "filters/screen/${key.path}.json"))
						.getOrNull()
						?: return@mapNotNull runNull {
							ErrorUtil.softError("Failed to locate screen filter at $key used by ${it.value.map { it.first }}")
						}
				val screenFilter =
					Firmament.tryDecodeJsonFromStream<ScreenFilter>(guiClassResource.inputStream)
						.getOrElse { ex ->
							ErrorUtil.softError("Failed to load screen filter at $key used by ${it.value.map { it.first }}", ex)
							return@mapNotNull null
						}
				ItemOverrideCollection(screenFilter, it.value.map { it.second })
			}
		logger.info("Loaded ${overrideResources.size} global item overrides")
		return CustomGuiTextureOverride(guiClasses)
	}

	var guiClassOverrides = CustomGuiTextureOverride(listOf())

	var matchingOverrides: Set<ItemOverrideCollection> = setOf()

	@Subscribe
	fun onOpenGui(event: ScreenChangeEvent) {
		val newTitle = event.new?.title ?: Text.empty()
		matchingOverrides = guiClassOverrides.classes
			.filterTo(mutableSetOf()) { it.screenFilter.title.matches(newTitle) }
	}

	@Subscribe
	fun replaceGlobalModel(event: CustomItemModelEvent) {
		val override = matchingOverrides
			.firstNotNullOfOrNull {
				it.overrides
					.asSequence()
					.filter { it.predicate.test(event.itemStack) }
					.map { it.model }
					.firstOrNull()
			}

		if (override != null)
			event.overrideIfExists(override)
	}


}

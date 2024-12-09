@file:UseSerializers(IdentifierSerializer::class)

package moe.nea.firmament.features.texturepack

import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.minecraft.client.render.entity.equipment.EquipmentModel
import net.minecraft.component.type.EquippableComponent
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.equipment.EquipmentAssetKeys
import net.minecraft.registry.RegistryKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.features.texturepack.CustomGlobalTextures.logger
import moe.nea.firmament.util.IdentifierSerializer
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.intoOptional
import moe.nea.firmament.util.skyBlockId

object CustomGlobalArmorOverrides {
	@Serializable
	data class ArmorOverride(
		@SerialName("item_ids")
		val itemIds: List<String>,
		val layers: List<ArmorOverrideLayer>? = null,
		val model: Identifier? = null,
		val overrides: List<ArmorOverrideOverride> = listOf(),
	) {
		@Transient
		lateinit var modelIdentifier: Identifier
		fun bake(manager: ResourceManager) {
			modelIdentifier = bakeModel(model, layers)
			overrides.forEach { it.bake(manager) }
		}

		init {
			require(layers != null || model != null) { "Either model or layers must be specified for armor override" }
			require(layers == null || model == null) { "Can't specify both model and layers for armor override" }
		}
	}

	@Serializable
	data class ArmorOverrideLayer(
		val tint: Boolean = false,
		val identifier: Identifier,
		val suffix: String = "",
	)

	@Serializable
	data class ArmorOverrideOverride(
		val predicate: FirmamentModelPredicate,
		val layers: List<ArmorOverrideLayer>? = null,
		val model: Identifier? = null,
	) {
		init {
			require(layers != null || model != null) { "Either model or layers must be specified for armor override override" }
			require(layers == null || model == null) { "Can't specify both model and layers for armor override override" }
		}

		@Transient
		lateinit var modelIdentifier: Identifier
		fun bake(manager: ResourceManager) {
			modelIdentifier = bakeModel(model, layers)
		}
	}


	private fun resolveComponent(slot: EquipmentSlot, model: Identifier): EquippableComponent {
		return EquippableComponent(
			slot,
			null,
			Optional.of(RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, model)),
			Optional.empty(),
			Optional.empty(), false, false, false
		)
	}

	val overrideCache =
		WeakCache.memoize<ItemStack, EquipmentSlot, Optional<EquippableComponent>>("ArmorOverrides") { stack, slot ->
			val id = stack.skyBlockId ?: return@memoize Optional.empty()
			val override = overrides[id.neuItem] ?: return@memoize Optional.empty()
			for (suboverride in override.overrides) {
				if (suboverride.predicate.test(stack)) {
					return@memoize resolveComponent(slot, suboverride.modelIdentifier).intoOptional()
				}
			}
			return@memoize resolveComponent(slot, override.modelIdentifier).intoOptional()
		}

	var overrides: Map<String, ArmorOverride> = mapOf()
	private var bakedOverrides: MutableMap<Identifier, EquipmentModel> = mutableMapOf()
	private val sentinelFirmRunning = AtomicInteger()

	private fun bakeModel(model: Identifier?, layers: List<ArmorOverrideLayer>?): Identifier {
		require(model == null || layers == null)
		if (model != null) {
			return model
		} else if (layers != null) {
			val idNumber = sentinelFirmRunning.incrementAndGet()
			val identifier = Identifier.of("firmament:sentinel/armor/$idNumber")
			val equipmentLayers = layers.map {
				EquipmentModel.Layer(
					it.identifier, if (it.tint) {
						Optional.of(EquipmentModel.Dyeable(Optional.empty()))
					} else {
						Optional.empty()
					},
					false
				)
			}
			bakedOverrides[identifier] = EquipmentModel(
				mapOf(
					EquipmentModel.LayerType.HUMANOID to equipmentLayers,
					EquipmentModel.LayerType.HUMANOID_LEGGINGS to equipmentLayers,
				)
			)
			return identifier
		} else {
			error("Either model or layers must be non null")
		}
	}


	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloader(object :
			                                       SinglePreparationResourceReloader<Map<String, ArmorOverride>>() {
			override fun prepare(manager: ResourceManager, profiler: Profiler): Map<String, ArmorOverride> {
				val overrideFiles = manager.findResources("overrides/armor_models") {
					it.namespace == "firmskyblock" && it.path.endsWith(".json")
				}
				val overrides = overrideFiles.mapNotNull {
					Firmament.tryDecodeJsonFromStream<ArmorOverride>(it.value.inputStream).getOrElse { ex ->
						logger.error("Failed to load armor texture override at ${it.key}", ex)
						null
					}
				}
				val associatedMap = overrides.flatMap { obj -> obj.itemIds.map { it to obj } }
					.toMap()
				associatedMap.forEach { it.value.bake(manager) }
				return associatedMap
			}

			override fun apply(prepared: Map<String, ArmorOverride>, manager: ResourceManager, profiler: Profiler) {
				bakedOverrides.clear()
				overrides = prepared
			}
		})
	}

	@JvmStatic
	fun overrideArmor(itemStack: ItemStack, slot: EquipmentSlot): Optional<EquippableComponent> {
		return overrideCache.invoke(itemStack, slot)
	}

	@JvmStatic
	fun overrideArmorLayer(id: Identifier): EquipmentModel? {
		return bakedOverrides[id]
	}

}

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.item.BlockModelWrapper
import net.minecraft.client.renderer.item.ItemModel
import net.minecraft.client.renderer.item.ItemModels
import net.minecraft.client.resources.model.ResolvableModel
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.ItemOwner
import net.minecraft.resources.Identifier
import moe.nea.firmament.features.texturepack.predicates.AndPredicate

class PredicateModel {
	data class Baked(
        val fallback: ItemModel,
        val overrides: List<Override>
	) : ItemModel {
		data class Override(
            val model: ItemModel,
            val predicate: FirmamentModelPredicate,
		)

		override fun update(
            state: ItemStackRenderState,
            stack: ItemStack,
            resolver: ItemModelResolver,
            displayContext: ItemDisplayContext,
            world: ClientLevel?,
            heldItemContext: ItemOwner?,
            seed: Int
		) {
			val model =
				overrides
					.findLast { it.predicate.test(stack, heldItemContext?.asLivingEntity()) }
					?.model
					?: fallback
			model.update(state, stack, resolver, displayContext, world, heldItemContext, seed)
		}
	}

	data class Unbaked(
        val fallback: ItemModel.Unbaked,
        val overrides: List<Override>,
	) : ItemModel.Unbaked {
		companion object {
			@JvmStatic
			fun fromLegacyJson(jsonObject: JsonObject, fallback: ItemModel.Unbaked): ItemModel.Unbaked {
				val legacyOverrides = jsonObject.getAsJsonArray("overrides") ?: return fallback
				val newOverrides = ArrayList<Override>()
				for (legacyOverride in legacyOverrides) {
					legacyOverride as JsonObject
					val overrideModel = Identifier.tryParse(legacyOverride.get("model")?.asString ?: continue) ?: continue
					val predicate = CustomModelOverrideParser.parsePredicates(legacyOverride.getAsJsonObject("predicate"))
					newOverrides.add(Override(
						BlockModelWrapper.Unbaked(overrideModel, listOf()),
						AndPredicate(predicate.toTypedArray())
					))
				}
				return Unbaked(fallback, newOverrides)
			}

			val OVERRIDE_CODEC: Codec<Override> = RecordCodecBuilder.create {
				it.group(
					ItemModels.CODEC.fieldOf("model").forGetter(Override::model),
					CustomModelOverrideParser.LEGACY_CODEC.fieldOf("predicate").forGetter(Override::predicate),
				).apply(it, Unbaked::Override)
			}
			val CODEC: MapCodec<Unbaked> =
				RecordCodecBuilder.mapCodec {
					it.group(
						ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback),
						OVERRIDE_CODEC.listOf().fieldOf("overrides").forGetter(Unbaked::overrides),
					).apply(it, ::Unbaked)
				}
		}

		data class Override(
            val model: ItemModel.Unbaked,
            val predicate: FirmamentModelPredicate,
		)

		override fun resolveDependencies(resolver: ResolvableModel.Resolver) {
			fallback.resolveDependencies(resolver)
			overrides.forEach { it.model.resolveDependencies(resolver) }
		}

		override fun type(): MapCodec<out Unbaked> {
			return CODEC
		}

		override fun bake(context: ItemModel.BakingContext): ItemModel {
			return Baked(
				fallback.bake(context),
				overrides.map { Baked.Override(it.model.bake(context), it.predicate) }
			)
		}
	}
}

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.item.ItemModelManager
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.client.render.item.model.BasicItemModel
import net.minecraft.client.render.item.model.ItemModel
import net.minecraft.client.render.item.model.ItemModelTypes
import net.minecraft.client.render.model.ResolvableModel
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemDisplayContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object HeadModelChooser {
	val IS_CHOOSING_HEAD_MODEL = ThreadLocal.withInitial { false }

	interface HasExplicitHeadModelMarker {
		fun markExplicitHead_Firmament()
		fun isExplicitHeadModel_Firmament(): Boolean
		companion object{
			@JvmStatic
			fun cast(state: ItemRenderState) = state as HasExplicitHeadModelMarker
		}
	}

	data class Baked(val head: ItemModel, val regular: ItemModel) : ItemModel {
		override fun update(
			state: ItemRenderState,
			stack: ItemStack?,
			resolver: ItemModelManager?,
			displayContext: ItemDisplayContext,
			world: ClientWorld?,
			user: LivingEntity?,
			seed: Int
		) {
			val instance =
				if (IS_CHOOSING_HEAD_MODEL.get()) {
					HasExplicitHeadModelMarker.cast(state).markExplicitHead_Firmament()
					head
				} else {
					regular
				}
			instance.update(state, stack, resolver, displayContext, world, user, seed)
		}
	}

	data class Unbaked(
		val head: ItemModel.Unbaked,
		val regular: ItemModel.Unbaked,
	) : ItemModel.Unbaked {
		override fun getCodec(): MapCodec<out ItemModel.Unbaked> {
			return CODEC
		}

		override fun bake(context: ItemModel.BakeContext): ItemModel {
			return Baked(
				head.bake(context),
				regular.bake(context)
			)
		}

		override fun resolve(resolver: ResolvableModel.Resolver) {
			head.resolve(resolver)
			regular.resolve(resolver)
		}

		companion object {
			@JvmStatic
			fun fromLegacyJson(jsonObject: JsonObject, unbakedModel: ItemModel.Unbaked): ItemModel.Unbaked {
				val model = jsonObject["firmament:head_model"] ?: return unbakedModel
				val modelUrl = model.asJsonPrimitive.asString
				val headModel = BasicItemModel.Unbaked(Identifier.of(modelUrl), listOf())
				return Unbaked(headModel, unbakedModel)
			}

			val CODEC = RecordCodecBuilder.mapCodec {
				it.group(
					ItemModelTypes.CODEC.fieldOf("head")
						.forGetter(Unbaked::head),
					ItemModelTypes.CODEC.fieldOf("regular")
						.forGetter(Unbaked::regular),
				).apply(it, ::Unbaked)
			}
		}
	}
}

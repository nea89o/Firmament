package moe.nea.firmament.features.misc

import util.render.CustomRenderPipelines
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.CustomRenderPassHelper

object CustomCapes {
	val identifier: String
		get() = "developer-capes"

	@Config
	object TConfig : ManagedConfig(identifier, Category.DEV) {
		val showCapes by toggle("show-cape") { true }
	}

	interface CustomCapeRenderer {
		fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		)
	}

	data class TexturedCapeRenderer(
		val location: Identifier
	) : CustomCapeRenderer {
		override fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		) {
			model(vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(location)))
		}
	}

	data class ParallaxedHighlightCapeRenderer(
		val template: Identifier,
		val background: Identifier,
		val overlay: Identifier,
		val animationSpeed: Duration,
	) : CustomCapeRenderer {
		override fun replaceRender(
			renderLayer: RenderLayer,
			vertexConsumerProvider: VertexConsumerProvider,
			matrixStack: MatrixStack,
			model: (VertexConsumer) -> Unit
		) {
			val animationValue = (startTime.passedTime() / animationSpeed).mod(1F)
			CustomRenderPassHelper(
				{ "Firmament Cape Renderer" },
				renderLayer.drawMode,
				renderLayer.vertexFormat,
				MC.instance.framebuffer,
				true,
			).use { renderPass ->
				renderPass.setPipeline(CustomRenderPipelines.PARALLAX_CAPE_SHADER)
				renderPass.setAllDefaultUniforms()
				renderPass.setUniform("Animation", 4) {
					it.putFloat(animationValue.toFloat())
				}
				renderPass.bindSampler("Sampler0", template)
				renderPass.bindSampler("Sampler1", background)
				renderPass.bindSampler("Sampler3", overlay)
				renderPass.uploadVertices(2048, model)
				renderPass.draw()
			}
		}
	}

	interface CapeStorage {
		companion object {
			@JvmStatic
			fun cast(playerEntityRenderState: PlayerEntityRenderState) =
				playerEntityRenderState as CapeStorage

		}

		var cape_firmament: CustomCape?
	}

	data class CustomCape(
		val id: String,
		val label: String,
		val render: CustomCapeRenderer,
	)

	enum class AllCapes(val label: String, val render: CustomCapeRenderer) {
		FIRMAMENT_ANIMATED(
			"Animated Firmament", ParallaxedHighlightCapeRenderer(
				Firmament.identifier("textures/cape/parallax_template.png"),
				Firmament.identifier("textures/cape/parallax_background.png"),
				Firmament.identifier("textures/cape/firmament_star.png"),
				110.seconds
			)
		),
		UNPLEASANT_GRADIENT(
			"unpleasant_gradient",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/unpleasant_gradient.png"))
		),
		FURFSKY_STATIC(
			"FurfSky",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/fsr_static.png"))
		),

		FIRMAMENT_STATIC(
			"Firmament",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/firm_static.png"))
		),
		HYPIXEL_PLUS(
			"Hypixel+",
			TexturedCapeRenderer(Firmament.identifier("textures/cape/h_plus.png"))
		),
		;

		val cape = CustomCape(name, label, render)
	}

	val byId = AllCapes.entries.associateBy { it.cape.id }
	val byUuid =
		listOf(
			listOf(
				Devs.nea to AllCapes.UNPLEASANT_GRADIENT,
				Devs.kath to AllCapes.FIRMAMENT_STATIC,
				Devs.jani to AllCapes.FIRMAMENT_ANIMATED,
				Devs.nat to AllCapes.FIRMAMENT_ANIMATED,
				Devs.HPlus.ic22487 to AllCapes.HYPIXEL_PLUS,
			),
			Devs.FurfSky.all.map { it to AllCapes.FURFSKY_STATIC },
		).flatten().flatMap { (dev, cape) -> dev.uuids.map { it to cape.cape } }.toMap()

	@JvmStatic
	fun render(
		playerEntityRenderState: PlayerEntityRenderState,
		vertexConsumer: VertexConsumer,
		renderLayer: RenderLayer,
		vertexConsumerProvider: VertexConsumerProvider,
		matrixStack: MatrixStack,
		model: (VertexConsumer) -> Unit
	) {
		val capeStorage = CapeStorage.cast(playerEntityRenderState)
		val firmCape = capeStorage.cape_firmament
		if (firmCape != null) {
			firmCape.render.replaceRender(renderLayer, vertexConsumerProvider, matrixStack, model)
		} else {
			model(vertexConsumer)
		}
	}

	@JvmStatic
	fun addCapeData(
		player: AbstractClientPlayerEntity,
		playerEntityRenderState: PlayerEntityRenderState
	) {
		val cape = if (TConfig.showCapes) byUuid[player.uuid] else null
		val capeStorage = CapeStorage.cast(playerEntityRenderState)
		if (cape == null) {
			capeStorage.cape_firmament = null
		} else {
			capeStorage.cape_firmament = cape
			playerEntityRenderState.skinTextures = SkinTextures(
				playerEntityRenderState.skinTextures.texture,
				playerEntityRenderState.skinTextures.textureUrl,
				Firmament.identifier("placeholder/fake_cape"),
				playerEntityRenderState.skinTextures.elytraTexture,
				playerEntityRenderState.skinTextures.model,
				playerEntityRenderState.skinTextures.secure,
			)
			playerEntityRenderState.capeVisible = true
		}
	}

	val startTime = TimeMark.now()
}

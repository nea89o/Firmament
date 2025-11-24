package moe.nea.firmament.features.misc

import util.render.CustomRenderPipelines
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.core.ClientAsset
import net.minecraft.resources.ResourceLocation
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
            renderLayer: RenderType,
            vertexConsumerProvider: MultiBufferSource,
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		)
	}

	data class TexturedCapeRenderer(
		val location: ResourceLocation
	) : CustomCapeRenderer {
		override fun replaceRender(
            renderLayer: RenderType,
            vertexConsumerProvider: MultiBufferSource,
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		) {
			model(vertexConsumerProvider.getBuffer(RenderType.entitySolid(location)))
		}
	}

	data class ParallaxedHighlightCapeRenderer(
        val template: ResourceLocation,
        val background: ResourceLocation,
        val overlay: ResourceLocation,
        val animationSpeed: Duration,
	) : CustomCapeRenderer {
		override fun replaceRender(
            renderLayer: RenderType,
            vertexConsumerProvider: MultiBufferSource,
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		) {
			val animationValue = (startTime.passedTime() / animationSpeed).mod(1F)
			CustomRenderPassHelper(
				{ "Firmament Cape Renderer" },
				renderLayer.mode(),
				renderLayer.format(),
				MC.instance.mainRenderTarget,
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
			fun cast(playerEntityRenderState: AvatarRenderState) =
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
	fun addCapeData(
        player: AbstractClientPlayer,
        playerEntityRenderState: AvatarRenderState
	) {
		if (true) return // TODO: see capefeaturerenderer mixin
		val cape = if (TConfig.showCapes) byUuid[player.uuid] else null
		val capeStorage = CapeStorage.cast(playerEntityRenderState)
		if (cape == null) {
			capeStorage.cape_firmament = null
		} else {
			capeStorage.cape_firmament = cape
			playerEntityRenderState.skin = PlayerSkin(
				playerEntityRenderState.skin.body,
				ClientAsset.ResourceTexture(Firmament.identifier("placeholder/fake_cape"), Firmament.identifier("placeholder/fake_cape")),
				playerEntityRenderState.skin.elytra,
				playerEntityRenderState.skin.model,
				playerEntityRenderState.skin.secure,
			)
			playerEntityRenderState.showCape = true
		}
	}

	val startTime = TimeMark.now()
}

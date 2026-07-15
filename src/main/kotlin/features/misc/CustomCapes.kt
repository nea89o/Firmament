package moe.nea.firmament.features.misc

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.player.AbstractClientPlayer
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import com.mojang.blaze3d.vertex.PoseStack
import java.util.Optional
import java.util.OptionalDouble
import org.lwjgl.system.MemoryStack
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.core.ClientAsset
import net.minecraft.resources.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.render.CustomRenderPipelines

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
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		)
	}

	data class TexturedCapeRenderer(
		val location: Identifier
	) : CustomCapeRenderer {
		override fun replaceRender(
            renderLayer: RenderType,
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		) {
//			model(vertexConsumerProvider.getBuffer(RenderType.entitySolid(location)))
		}
	}

	data class ParallaxedHighlightCapeRenderer(
        val template: Identifier,
        val background: Identifier,
        val overlay: Identifier,
        val animationSpeed: Duration,
	) : CustomCapeRenderer {
		override fun replaceRender(
            renderLayer: RenderType,
            matrixStack: PoseStack,
            model: (VertexConsumer) -> Unit
		) {
			val animationValue = (startTime.passedTime() / animationSpeed).mod(1F)

			val renderTarget = MC.gameRenderer.mainRenderTarget();

			// This vertex buffer management is terrible and is probably not good performance wise, but it will work
			// which is what I want
			val bufferBuilder = ByteBufferBuilder(2048);
			val buffer = BufferBuilder(bufferBuilder, CustomRenderPipelines.PARALLAX_CAPE.primitiveTopology, CustomRenderPipelines.PARALLAX_CAPE.getVertexFormatBinding(0)!!);
			model.invoke(buffer);

			val meshData = buffer.buildOrThrow();
			val vertexBuffer = RenderSystem.getDevice().createBuffer({ "Custom Cape Buffer" }, GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());

			val sequentialBuffer = RenderSystem.getSequentialBuffer(CustomRenderPipelines.PARALLAX_CAPE.primitiveTopology);
			val indexBuffer = sequentialBuffer.getBuffer(meshData.drawState().indexCount());
			val indexType = sequentialBuffer.type();

			// This uniform buffer management is also bad but whatever
			val animationUniformBufferSize = Std140SizeCalculator().putFloat().get();
			val animationUniformBuffer = RenderSystem.getDevice().createBuffer({ "Animation Uniform" }, GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST, animationUniformBufferSize.toLong());

			MemoryStack.stackPush().use { stack ->
				val byteBuffer = Std140Builder.onStack(stack, animationUniformBufferSize)
					.putFloat(animationValue.toFloat())
					.get();
				RenderSystem.getDevice().createCommandEncoder().writeToBuffer(animationUniformBuffer.slice(), byteBuffer);
			}

			RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				{ "Firmament Cape Renderer" },
				renderTarget.colorTextureView!!,
				Optional.empty(),
				if (renderTarget.useDepth) renderTarget.depthTextureView!! else null,
				OptionalDouble.empty()
			).use { renderPass ->
				renderPass.setPipeline(CustomRenderPipelines.PARALLAX_CAPE)

				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("Animation", animationUniformBuffer);

				val templateTex = MC.textureManager.getTexture(template);
				renderPass.bindTexture("Sampler0", templateTex.textureView, templateTex.sampler);

				val backgroundTex = MC.textureManager.getTexture(background);
				renderPass.bindTexture("Sampler1", backgroundTex.textureView, backgroundTex.sampler);

				val overlayTex = MC.textureManager.getTexture(overlay);
				renderPass.bindTexture("Sampler2", overlayTex.textureView, overlayTex.sampler);

				renderPass.setIndexBuffer(indexBuffer, indexType);
				renderPass.setVertexBuffer(0, vertexBuffer.slice());

				renderPass.drawIndexed(meshData.drawState().indexCount(), 1, 0, 0, 0);
			}

			bufferBuilder.close();
			meshData.close();
			vertexBuffer.close();
			animationUniformBuffer.close();
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

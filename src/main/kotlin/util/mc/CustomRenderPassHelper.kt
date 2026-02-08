package moe.nea.firmament.util.mc

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.OptionalDouble
import java.util.OptionalInt
import org.joml.Vector3f
import org.joml.Vector4f
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.MeshData
import net.minecraft.client.renderer.texture.AbstractTexture
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import org.joml.Matrix4f
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC


class CustomRenderPassHelper(
	val labelSupplier: () -> String,
	val drawMode: VertexFormat.Mode,
	val vertexFormat: VertexFormat,
	val frameBuffer: RenderTarget,
	val hasDepth: Boolean,
) : AutoCloseable {
	private val scope = mutableListOf<AutoCloseable>()
	private val preparations = mutableListOf<(RenderPass) -> Unit>()
	val device = RenderSystem.getDevice()
	private var hasPipelineAction = false
	private var hasSetDefaultUniforms = false
	val commandEncoder = device.createCommandEncoder()
	fun setPipeline(pipeline: RenderPipeline) {
		ErrorUtil.softCheck("Already has a pipeline", !hasPipelineAction)
		hasPipelineAction = true
		queueAction {
			it.setPipeline(pipeline)
		}
	}

	fun bindSampler(name: String, texture: Identifier) {
		bindSampler(name, MC.textureManager.getTexture(texture))
	}

	fun bindSampler(name: String, texture: AbstractTexture) {
		queueAction { it.bindTexture(name, texture.textureView, texture.sampler) }
	}


	fun dontSetDefaultUniforms() {
		hasSetDefaultUniforms = true
	}

	fun setAllDefaultUniforms() {
		hasSetDefaultUniforms = true
		queueAction {
			RenderSystem.bindDefaultUniforms(it)
		}
		setUniform(
			"DynamicTransforms", RenderSystem.getDynamicUniforms()
				.writeTransform(
					RenderSystem.getModelViewMatrix(),
					Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
					Vector3f(), // TODO: 1.21.10
					Matrix4f()
				)
		)
	}

	fun setUniform(name: String, slice: GpuBufferSlice) = queueAction { it.setUniform(name, slice) }
	fun setUniform(name: String, slice: GpuBuffer) = queueAction { it.setUniform(name, slice) }

	fun setUniform(name: String, size: Int, labelSupplier: () -> String = { name }, init: (Std140Builder) -> Unit) {
		val buffer = createUniformBuffer(labelSupplier, allocateByteBuf(size, init))
		setUniform(name, buffer)
	}

	var vertices: MeshData? = null

	fun uploadVertices(size: Int, init: (BufferBuilder) -> Unit) {
		uploadVertices(
			BufferBuilder(queueClose(ByteBufferBuilder(size)), drawMode, vertexFormat)
				.also(init)
				.buildOrThrow()
		)
	}

	fun uploadVertices(buffer: MeshData) {
		queueClose(buffer)
		ErrorUtil.softCheck("Vertices have already been uploaded", vertices == null)
		vertices = buffer
		val vertexBuffer = vertexFormat.uploadImmediateVertexBuffer(buffer.vertexBuffer())
		val indexBufferConstructor = RenderSystem.getSequentialBuffer(drawMode)
		val indexBuffer = indexBufferConstructor.getBuffer(buffer.drawState().indexCount)
		queueAction {
			it.setIndexBuffer(indexBuffer, indexBufferConstructor.type())
			it.setVertexBuffer(0, vertexBuffer)
		}
	}

	fun createUniformBuffer(labelSupplier: () -> String, buffer: ByteBuffer): GpuBuffer {
		return queueClose(
			device.createBuffer(
				labelSupplier::invoke,
				GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_READ,
				buffer
			)
		)
	}

	fun allocateByteBuf(size: Int, init: (Std140Builder) -> Unit): ByteBuffer {
		return Std140Builder.intoBuffer( // TODO: i really dont know about this 16 align? but it seems to be generally correct.
			ByteBuffer
				.allocateDirect(Mth.roundToward(size, 16))
				.order(ByteOrder.nativeOrder())
		).also(init).get()
	}

	fun queueAction(action: (RenderPass) -> Unit) {
		preparations.add(action)
	}

	fun <T : AutoCloseable> queueClose(t: T): T = t.also { scope.add(it) }
	override fun close() {
		scope.reversed().forEach { it.close() }
	}

	object DrawToken

	fun draw(): DrawToken {
		val vertexData = (ErrorUtil.notNullOr(vertices, "No vertex data uploaded") { return DrawToken })
		ErrorUtil.softCheck("Missing default uniforms", hasSetDefaultUniforms)
		ErrorUtil.softCheck("Missing a pipeline", hasPipelineAction)
		val renderPass = queueClose(
			commandEncoder.createRenderPass(
				labelSupplier::invoke,
				RenderSystem.outputColorTextureOverride ?: frameBuffer.colorTextureView!!,
				OptionalInt.empty(),
				(RenderSystem.outputDepthTextureOverride
					?: frameBuffer.depthTextureView).takeIf { frameBuffer.useDepth && hasDepth },
				OptionalDouble.empty()
			)
		)
		preparations.forEach { it(renderPass) }
		renderPass.drawIndexed(
			0,
			0,
			vertexData.drawState().indexCount,
			1
		)
		return DrawToken
	}
}

package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import io.github.notenoughupdates.moulconfig.platform.next
import java.lang.Math.pow
import org.joml.Matrix4f
import org.joml.Vector3f
import util.render.CustomRenderLayers
import net.minecraft.client.render.Camera
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.IntUtil.toRGBA
import moe.nea.firmament.util.MC

@RenderContextDSL
class RenderInWorldContext private constructor(
	val matrixStack: MatrixStack,
	private val camera: Camera,
	private val tickCounter: RenderTickCounter,
	val vertexConsumers: VertexConsumerProvider.Immediate,
) {


	@Deprecated("stateful color management is no longer a thing")
	fun color(color: me.shedaniel.math.Color) {
		color(color.red / 255F, color.green / 255f, color.blue / 255f, color.alpha / 255f)
	}

	@Deprecated("stateful color management is no longer a thing")
	fun color(red: Float, green: Float, blue: Float, alpha: Float) {
		RenderSystem.setShaderColor(red, green, blue, alpha)
	}

	fun block(blockPos: BlockPos, color: Int) {
		matrixStack.push()
		matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
		buildCube(matrixStack.peek().positionMatrix, vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS), color)
		matrixStack.pop()
	}

	enum class VerticalAlign {
		TOP, BOTTOM, CENTER;

		fun align(index: Int, count: Int): Float {
			return when (this) {
				CENTER -> (index - count / 2F) * (1 + MC.font.fontHeight.toFloat())
				BOTTOM -> (index - count) * (1 + MC.font.fontHeight.toFloat())
				TOP -> (index) * (1 + MC.font.fontHeight.toFloat())
			}
		}
	}

	fun waypoint(position: BlockPos, vararg label: Text) {
		text(
			position.toCenterPos(),
			*label,
			Text.literal("§e${FirmFormatters.formatDistance(MC.player?.pos?.distanceTo(position.toCenterPos()) ?: 42069.0)}"),
			background = 0xAA202020.toInt()
		)
	}

	fun withFacingThePlayer(position: Vec3d, block: FacingThePlayerContext.() -> Unit) {
		matrixStack.push()
		matrixStack.translate(position.x, position.y, position.z)
		val actualCameraDistance = position.distanceTo(camera.pos)
		val distanceToMoveTowardsCamera = if (actualCameraDistance < 10) 0.0 else -(actualCameraDistance - 10.0)
		val vec = position.subtract(camera.pos).multiply(distanceToMoveTowardsCamera / actualCameraDistance)
		matrixStack.translate(vec.x, vec.y, vec.z)
		matrixStack.multiply(camera.rotation)
		matrixStack.scale(0.025F, -0.025F, 1F)

		FacingThePlayerContext(this).run(block)

		matrixStack.pop()
		vertexConsumers.drawCurrentLayer()
	}

	fun sprite(position: Vec3d, sprite: Sprite, width: Int, height: Int) {
		texture(
			position, sprite.atlasId, width, height, sprite.minU, sprite.minV, sprite.maxU, sprite.maxV
		)
	}

	fun texture(
		position: Vec3d, texture: Identifier, width: Int, height: Int,
		u1: Float, v1: Float,
		u2: Float, v2: Float,
	) {
		withFacingThePlayer(position) {
			texture(texture, width, height, u1, v1, u2, v2)
		}
	}

	fun text(
		position: Vec3d,
		vararg texts: Text,
		verticalAlign: VerticalAlign = VerticalAlign.CENTER,
		background: Int = 0x70808080
	) {
		withFacingThePlayer(position) {
			text(*texts, verticalAlign = verticalAlign, background = background)
		}
	}

	fun tinyBlock(vec3d: Vec3d, size: Float, color: Int) {
		matrixStack.push()
		matrixStack.translate(vec3d.x, vec3d.y, vec3d.z)
		matrixStack.scale(size, size, size)
		matrixStack.translate(-.5, -.5, -.5)
		buildCube(matrixStack.peek().positionMatrix, vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS), color)
		matrixStack.pop()
		vertexConsumers.draw()
	}

	fun wireframeCube(blockPos: BlockPos, lineWidth: Float = 10F) {
		val buf = vertexConsumers.getBuffer(RenderLayer.LINES)
		matrixStack.push()
		// TODO: this does not render through blocks (or water layers) anymore
		RenderSystem.lineWidth(lineWidth / pow(camera.pos.squaredDistanceTo(blockPos.toCenterPos()), 0.25).toFloat())
		matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
		buildWireFrameCube(matrixStack.peek(), buf)
		matrixStack.pop()
		vertexConsumers.draw()
	}

	fun line(vararg points: Vec3d, lineWidth: Float = 10F) {
		line(points.toList(), lineWidth)
	}

	fun tracer(toWhere: Vec3d, lineWidth: Float = 3f) {
		val cameraForward = Vector3f(0f, 0f, -1f).rotate(camera.rotation)
		line(camera.pos.add(Vec3d(cameraForward)), toWhere, lineWidth = lineWidth)
	}

	fun line(points: List<Vec3d>, lineWidth: Float = 10F) {
		RenderSystem.lineWidth(lineWidth)
		val buffer = vertexConsumers.getBuffer(CustomRenderLayers.LINES)

		val matrix = matrixStack.peek()
		var lastNormal: Vector3f? = null
		points.zipWithNext().forEach { (a, b) ->
			val normal = Vector3f(b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
				.sub(a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
				.normalize()
			val lastNormal0 = lastNormal ?: normal
			lastNormal = normal
			buffer.vertex(matrix.positionMatrix, a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
				.color(-1)
				.normal(matrix, lastNormal0.x, lastNormal0.y, lastNormal0.z)
				.next()
			buffer.vertex(matrix.positionMatrix, b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
				.color(-1)
				.normal(matrix, normal.x, normal.y, normal.z)
				.next()
		}

	}
	// TODO: put the favourite icons in front of items again

	companion object {
		private fun doLine(
			matrix: MatrixStack.Entry,
			buf: VertexConsumer,
			i: Float,
			j: Float,
			k: Float,
			x: Float,
			y: Float,
			z: Float
		) {
			val normal = Vector3f(x, y, z)
				.sub(i, j, k)
				.normalize()
			buf.vertex(matrix.positionMatrix, i, j, k)
				.normal(matrix, normal.x, normal.y, normal.z)
				.color(-1)
				.next()
			buf.vertex(matrix.positionMatrix, x, y, z)
				.normal(matrix, normal.x, normal.y, normal.z)
				.color(-1)
				.next()
		}


		private fun buildWireFrameCube(matrix: MatrixStack.Entry, buf: VertexConsumer) {
			for (i in 0..1) {
				for (j in 0..1) {
					val i = i.toFloat()
					val j = j.toFloat()
					doLine(matrix, buf, 0F, i, j, 1F, i, j)
					doLine(matrix, buf, i, 0F, j, i, 1F, j)
					doLine(matrix, buf, i, j, 0F, i, j, 1F)
				}
			}
		}

		private fun buildCube(matrix: Matrix4f, buf: VertexConsumer, colorInt: Int) {
			val (r, g, b, a) = colorInt.toRGBA()

			// Y-
			buf.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 0F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 0F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 0F, 0F).color(r, g, b, a)
			// Y+
			buf.vertex(matrix, 0F, 1F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 1F, 1F).color(r, g, b, a)
			// X-
			buf.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 0F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 1F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 1F, 0F).color(r, g, b, a)
			// X+
			buf.vertex(matrix, 1F, 0F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 0F, 1F).color(r, g, b, a)
			// Z-
			buf.vertex(matrix, 0F, 0F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 0F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 0F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 1F, 0F).color(r, g, b, a)
			// Z+
			buf.vertex(matrix, 0F, 0F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 0F, 1F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 1F, 1F).color(r, g, b, a)
			buf.vertex(matrix, 1F, 0F, 1F).color(r, g, b, a)
		}


		fun renderInWorld(event: WorldRenderLastEvent, block: RenderInWorldContext. () -> Unit) {
			// TODO: there should be *no more global state*. the only thing we should be doing is render layers. that includes settings like culling, blending, shader color, and depth testing
			// For now i will let these functions remain, but this needs to go before i do a full (non-beta) release
//			RenderSystem.disableDepthTest()
//			RenderSystem.enableBlend()
//			RenderSystem.defaultBlendFunc()
//			RenderSystem.disableCull()

			event.matrices.push()
			event.matrices.translate(-event.camera.pos.x, -event.camera.pos.y, -event.camera.pos.z)

			val ctx = RenderInWorldContext(
				event.matrices,
				event.camera,
				event.tickCounter,
				event.vertexConsumers
			)

			block(ctx)

			event.matrices.pop()
			event.vertexConsumers.draw()
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
		}
	}
}



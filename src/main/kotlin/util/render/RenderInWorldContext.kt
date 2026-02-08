package moe.nea.firmament.util.render

import org.joml.Matrix4f
import org.joml.Vector3f
import util.render.CustomRenderLayers
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC

@RenderContextDSL
class RenderInWorldContext private constructor(
	val matrixStack: PoseStack,
	private val camera: CameraRenderState,
	val vertexConsumers: MultiBufferSource.BufferSource,
) {
	fun block(blockPos: BlockPos, color: Int) {
		matrixStack.pushPose()
		matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
		buildCube(matrixStack.last().pose(), vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS), color)
		matrixStack.popPose()
	}

	fun box(aabb: AABB, color: Int) {
		matrixStack.pushPose()
		matrixStack.translate(aabb.minX, aabb.minY, aabb.minZ)
		matrixStack.scale(aabb.xsize.toFloat(), aabb.ysize.toFloat(), aabb.zsize.toFloat())
		buildCube(matrixStack.last().pose(), vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS), color)
		matrixStack.popPose()
	}

	fun sharedVoxelSurface(blocks: Set<BlockPos>, color: Int) {
		val m = BlockPos.MutableBlockPos()
		val l = vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS)
		blocks.forEach {
			matrixStack.pushPose()
			matrixStack.translate(it.x.toFloat(), it.y.toFloat(), it.z.toFloat())
			val p = matrixStack.last().pose()
			m.set(it)
			if (m.setX(it.x + 1) !in blocks) {
				buildFaceXP(p, l, color)
			}
			if (m.setX(it.x - 1) !in blocks) {
				buildFaceXN(p, l, color)
			}
			m.set(it)
			if (m.setY(it.y + 1) !in blocks) {
				buildFaceYP(p, l, color)
			}
			if (m.setY(it.y - 1) !in blocks) {
				buildFaceYN(p, l, color)
			}
			m.set(it)
			if (m.setZ(it.z + 1) !in blocks) {
				buildFaceZP(p, l, color)
			}
			if (m.setZ(it.z - 1) !in blocks) {
				buildFaceZN(p, l, color)
			}
			matrixStack.popPose()
		}
	}

	enum class VerticalAlign {
		TOP, BOTTOM, CENTER;

		fun align(index: Int, count: Int): Float {
			return when (this) {
				CENTER -> (index - count / 2F) * (1 + MC.font.lineHeight.toFloat())
				BOTTOM -> (index - count) * (1 + MC.font.lineHeight.toFloat())
				TOP -> (index) * (1 + MC.font.lineHeight.toFloat())
			}
		}
	}

	fun waypoint(position: BlockPos, vararg label: Component) {
		text(
			position.center,
			*label,
			Component.literal("§e${FirmFormatters.formatDistance(MC.player?.position?.distanceTo(position.center) ?: 42069.0)}"),
			background = 0xAA202020.toInt()
		)
	}

	fun withFacingThePlayer(position: Vec3, block: FacingThePlayerContext.() -> Unit) {
		matrixStack.pushPose()
		matrixStack.translate(position.x, position.y, position.z)
		val actualCameraDistance = position.distanceTo(camera.pos)
		val distanceToMoveTowardsCamera = if (actualCameraDistance < 10) 0.0 else -(actualCameraDistance - 10.0)
		val vec = position.subtract(camera.pos).scale(distanceToMoveTowardsCamera / actualCameraDistance)
		matrixStack.translate(vec.x, vec.y, vec.z)
		matrixStack.mulPose(camera.orientation)
		matrixStack.scale(0.025F, -0.025F, 1F)

		FacingThePlayerContext(this).run(block)

		matrixStack.popPose()
		vertexConsumers.endLastBatch()
	}

	fun sprite(position: Vec3, sprite: TextureAtlasSprite, width: Int, height: Int) {
		texture(
			position, sprite.atlasLocation(), width, height, sprite.u0, sprite.v0, sprite.u1, sprite.v1
		)
	}

	fun texture(
		position: Vec3, texture: Identifier, width: Int, height: Int,
		u1: Float, v1: Float,
		u2: Float, v2: Float,
	) {
		withFacingThePlayer(position) {
			texture(texture, width, height, u1, v1, u2, v2)
		}
	}

	fun text(
		position: Vec3,
		vararg texts: Component,
		verticalAlign: VerticalAlign = VerticalAlign.CENTER,
		background: Int = 0x70808080
	) {
		withFacingThePlayer(position) {
			text(*texts, verticalAlign = verticalAlign, background = background)
		}
	}

	fun tinyBlock(vec3d: Vec3, size: Float, color: Int) {
		matrixStack.pushPose()
		matrixStack.translate(vec3d.x, vec3d.y, vec3d.z)
		matrixStack.scale(size, size, size)
		matrixStack.translate(-.5, -.5, -.5)
		buildCube(matrixStack.last().pose(), vertexConsumers.getBuffer(CustomRenderLayers.COLORED_QUADS), color)
		matrixStack.popPose()
		vertexConsumers.endBatch()
	}

	fun wireframeCube(blockPos: BlockPos, lineWidth: Float = 10F) {
		val buf = vertexConsumers.getBuffer(RenderTypes.LINES)
		matrixStack.pushPose()
		// TODO: add color arg to this
		// TODO: this does not render through blocks (or water layers) anymore
		val offset = 1 / 512F
		matrixStack.translate(
			blockPos.x.toFloat() - offset,
			blockPos.y.toFloat() - offset,
			blockPos.z.toFloat() - offset
		)
		val scale = 1 + 2 * offset
		matrixStack.scale(scale, scale, scale)

		buildWireFrameCube(matrixStack.last(), buf, lineWidth)
		matrixStack.popPose()
		vertexConsumers.endBatch()
	}

	fun line(vararg points: Vec3, color: Int, lineWidth: Float = 10F) {
		line(points.toList(), color, lineWidth)
	}

	fun tracer(toWhere: Vec3, color: Int, lineWidth: Float = 3f) {
		val cameraForward = Vector3f(0f, 0f, -1f).rotate(camera.orientation)
		line(camera.pos.add(Vec3(cameraForward)), toWhere, color = color, lineWidth = lineWidth)
	}

	fun line(points: List<Vec3>, color: Int, lineWidth: Float = 10F) {
		val buffer = vertexConsumers.getBuffer(RenderTypes.LINES)

		val matrix = matrixStack.last()
		var lastNormal: Vector3f? = null
		points.zipWithNext().forEach { (a, b) ->
			val normal = Vector3f(b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
				.sub(a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
				.normalize()
			val lastNormal0 = lastNormal ?: normal
			lastNormal = normal
			buffer.addVertex(matrix.pose(), a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
				.setColor(color)
				.setLineWidth(lineWidth)
				.setNormal(matrix, lastNormal0.x, lastNormal0.y, lastNormal0.z)

			buffer.addVertex(matrix.pose(), b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
				.setColor(color)
				.setLineWidth(lineWidth)
				.setNormal(matrix, normal.x, normal.y, normal.z)

		}

	}
	// TODO: put the favourite icons in front of items again

	companion object {
		private fun doLine( // i swear its legal
			matrix: PoseStack.Pose,
			buf: VertexConsumer,
			i: Float,
			j: Float,
			k: Float,
			x: Float,
			y: Float,
			z: Float,
			lineWidth: Float
		) {
			val normal = Vector3f(x, y, z)
				.sub(i, j, k)
				.normalize()
			buf.addVertex(matrix.pose(), i, j, k)
				.setNormal(matrix, normal.x, normal.y, normal.z)
				.setColor(-1)
				.setLineWidth(lineWidth)

			buf.addVertex(matrix.pose(), x, y, z)
				.setNormal(matrix, normal.x, normal.y, normal.z)
				.setColor(-1)
				.setLineWidth(lineWidth)

		}


		private fun buildWireFrameCube(matrix: PoseStack.Pose, buf: VertexConsumer, lineWidth: Float) {
			for (i in 0..1) {
				for (j in 0..1) {
					val i = i.toFloat()
					val j = j.toFloat()
					doLine(matrix, buf, 0F, i, j, 1F, i, j, lineWidth)
					doLine(matrix, buf, i, 0F, j, i, 1F, j, lineWidth)
					doLine(matrix, buf, i, j, 0F, i, j, 1F, lineWidth)
				}
			}
		}

		private fun buildFaceZP(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 0F, 0F, 1F).setColor(rgba)
			buf.addVertex(matrix, 0F, 1F, 1F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 1F).setColor(rgba)
			buf.addVertex(matrix, 1F, 0F, 1F).setColor(rgba)
		}

		private fun buildFaceZN(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 0F, 0F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 0F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 0F).setColor(rgba)
			buf.addVertex(matrix, 0F, 1F, 0F).setColor(rgba)
		}

		private fun buildFaceXP(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 1F, 0F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 1F).setColor(rgba)
			buf.addVertex(matrix, 1F, 0F, 1F).setColor(rgba)
		}

		private fun buildFaceXN(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 0F, 0F, 0F).setColor(rgba)
			buf.addVertex(matrix, 0F, 0F, 1F).setColor(rgba)
			buf.addVertex(matrix, 0F, 1F, 1F).setColor(rgba)
			buf.addVertex(matrix, 0F, 1F, 0F).setColor(rgba)
		}

		private fun buildFaceYN(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 0F, 0F, 0F).setColor(rgba)
			buf.addVertex(matrix, 0F, 0F, 1F).setColor(rgba)
			buf.addVertex(matrix, 1F, 0F, 1F).setColor(rgba)
			buf.addVertex(matrix, 1F, 0F, 0F).setColor(rgba)
		}

		private fun buildFaceYP(matrix: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buf.addVertex(matrix, 0F, 1F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 0F).setColor(rgba)
			buf.addVertex(matrix, 1F, 1F, 1F).setColor(rgba)
			buf.addVertex(matrix, 0F, 1F, 1F).setColor(rgba)
		}

		private fun buildCube(matrix4f: Matrix4f, buf: VertexConsumer, rgba: Int) {
			buildFaceXP(matrix4f, buf, rgba)
			buildFaceXN(matrix4f, buf, rgba)
			buildFaceYP(matrix4f, buf, rgba)
			buildFaceYN(matrix4f, buf, rgba)
			buildFaceZP(matrix4f, buf, rgba)
			buildFaceZN(matrix4f, buf, rgba)
		}

		fun renderInWorld(event: WorldRenderLastEvent, block: RenderInWorldContext. () -> Unit) {

			event.matrices.pushPose()
			event.matrices.translate(-event.camera.pos.x, -event.camera.pos.y, -event.camera.pos.z)

			val ctx = RenderInWorldContext(
				event.matrices,
				event.camera,
				event.vertexConsumers
			)

			block(ctx)

			event.matrices.popPose()
			event.vertexConsumers.endBatch()
		}
	}
}



package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import org.joml.Vector3f
import util.render.CustomRenderLayers
import kotlin.math.abs
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.TextureFilteringMethod
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier
import moe.nea.firmament.util.MC

fun GuiGraphics.isUntranslatedGuiDrawContext(): Boolean {
	return pose().m00 == 1F && pose().m11 == 1f && pose().m01 == 0F && pose().m10 == 0F && pose().m20 == 0F && pose().m21 == 0F
}

@Deprecated("Use the other drawGuiTexture")
fun GuiGraphics.drawGuiTexture(
	x: Int, y: Int, z: Int, width: Int, height: Int, sprite: Identifier
) = this.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun GuiGraphics.drawGuiTexture(
	sprite: Identifier,
	x: Int, y: Int, width: Int, height: Int
) = this.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun GuiGraphics.drawTexture(
	sprite: Identifier,
	x: Int,
	y: Int,
	u: Float,
	v: Float,
	width: Int,
	height: Int,
	textureWidth: Int,
	textureHeight: Int
) {
	this.blit(
		RenderPipelines.GUI_TEXTURED,
		sprite,
		x,
		y,
		u,
		v,
		width,
		height,
		width,
		height,
		textureWidth,
		textureHeight
	)
}

data class LineRenderState(
	override val x1: Int,
	override val x2: Int,
	override val y1: Int,
	override val y2: Int,
	override val scale: Float,
	override val bounds: ScreenRectangle,
	val lineWidth: Float,
	val w: Int,
	val h: Int,
	val color: Int,
	val direction: LineDirection,
) : MultiSpecialGuiRenderState() {
	enum class LineDirection {
		TOP_LEFT_TO_BOTTOM_RIGHT,
		BOTTOM_LEFT_TO_TOP_RIGHT,
	}

	override fun createRenderer(vertexConsumers: MultiBufferSource.BufferSource): MultiSpecialGuiRenderer<out MultiSpecialGuiRenderState> {
		return LineRenderer(vertexConsumers)
	}

	override val scissorArea = null
}

class LineRenderer(vertexConsumers: MultiBufferSource.BufferSource) :
	MultiSpecialGuiRenderer<LineRenderState>(vertexConsumers) {
	override fun getRenderStateClass(): Class<LineRenderState> {
		return LineRenderState::class.java
	}

	override fun getTranslateY(height: Int, windowScaleFactor: Int): Float {
		return height / 2F
	}

	override fun renderToTexture(
		state: LineRenderState,
		matrices: PoseStack
	) {
		val gr = MC.instance.gameRenderer
		val client = MC.instance

		gr.globalSettingsUniform
			.update(
				state.bounds.width,
				state.bounds.height,
				client.options.glintStrength().get(),
				client.level?.gameTime ?: 0L,
				client.deltaTracker,
				client.options.menuBackgroundBlurriness,
				gr.mainCamera,
				client.options.textureFiltering().get() == TextureFilteringMethod.RGSS
			) // TODO: is this viewport mangling still needed with the new line shader in 1.21.11

		val buf = bufferSource.getBuffer(RenderTypes.LINES)
		val matrix = matrices.last()
		val wh = state.w / 2F
		val hh = state.h / 2F
		val lowX = -wh
		val lowY = if (state.direction == LineRenderState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT) hh else -hh
		val highX = wh
		val highY = -lowY
		val norm = Vector3f(highX - lowX, highY - lowY, 0F).normalize()
		buf.addVertex(matrix, lowX, lowY, 0F).setColor(state.color)
			.setNormal(matrix, norm)
			.setLineWidth(state.lineWidth)
		buf.addVertex(matrix, highX, highY, 0F).setColor(state.color)
			.setNormal(matrix, norm)
			.setLineWidth(state.lineWidth)
		bufferSource.endBatch()
		gr.globalSettingsUniform
			.update(
				client.window.width,
				client.window.height,
				client.options.glintStrength().get(),
				client.level?.gameTime ?: 0L,
				client.deltaTracker,
				client.options.menuBackgroundBlurriness,
				gr.mainCamera,
				client.options.textureFiltering().get() == TextureFilteringMethod.RGSS
			)
	}

	override fun getTextureLabel(): String {
		return "Firmament Line Renderer"
	}
}


fun GuiGraphics.drawAlignedBox(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Int) {
	vLine(fromX, fromY, toY, color)
	vLine(toY, fromY, toY, color)
	hLine(fromX, toX, fromY, color)
	hLine(fromX, toX, toY, color)
}

fun GuiGraphics.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color, lineWidth: Float = 1F) {
	if (toY < fromY) {
		drawLine(toX, toY, fromX, fromY, color)
		return
	}
	val originalRect = ScreenRectangle(
		minOf(fromX, toX), minOf(toY, fromY),
		abs(toX - fromX), abs(toY - fromY)
	).transformAxisAligned(pose())
	val expansionFactor = 3
	val rect = ScreenRectangle(
		originalRect.left() - expansionFactor,
		originalRect.top() - expansionFactor,
		originalRect.width + expansionFactor * 2,
		originalRect.height + expansionFactor * 2
	)
	// TODO: expand the bounds so that the thickness of the line can be used
	// TODO: fix this up to work with scissorarea
	guiRenderState.submitPicturesInPictureState(
		LineRenderState(
			rect.left(), rect.right(), rect.top(), rect.bottom(), 1F, rect, lineWidth,
			originalRect.width, originalRect.height, color.color,
			if (fromX < toX) LineRenderState.LineDirection.TOP_LEFT_TO_BOTTOM_RIGHT else LineRenderState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT
		)
	)
}


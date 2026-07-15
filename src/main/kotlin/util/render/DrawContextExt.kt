package moe.nea.firmament.util.render

import me.shedaniel.math.Color
import kotlin.math.abs
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.resources.Identifier
import moe.nea.firmament.util.render.state.gui.RenderLineState

fun GuiGraphicsExtractor.isUntranslatedGuiDrawContext(): Boolean {
	return pose().m00 == 1F && pose().m11 == 1f && pose().m01 == 0F && pose().m10 == 0F && pose().m20 == 0F && pose().m21 == 0F
}

@Deprecated("Use the other drawGuiTexture")
fun GuiGraphicsExtractor.drawGuiTexture(
	x: Int, y: Int, z: Int, width: Int, height: Int, sprite: Identifier
) = this.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun GuiGraphicsExtractor.drawGuiTexture(
	sprite: Identifier,
	x: Int, y: Int, width: Int, height: Int
) = this.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)

fun GuiGraphicsExtractor.drawTexture(
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

fun GuiGraphicsExtractor.drawAlignedBox(fromX: Int, fromY: Int, width: Int, height: Int, color: Int) {
	val toY = fromY + height
	val toX = fromX + width
	verticalLine(fromX, fromY, toY, color)
	verticalLine(toX, fromY, toY, color)
	horizontalLine(fromX, toX, fromY, color)
	horizontalLine(fromX, toX, toY, color)
}

fun GuiGraphicsExtractor.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color, lineWidth: Float = 1F) {
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
	guiRenderState.addPicturesInPictureState(
		RenderLineState(
			rect.left(), rect.top(), rect.right(), rect.bottom(), 1F, rect, lineWidth,
			originalRect.width, originalRect.height, color.color,
			if (fromX < toX) RenderLineState.LineDirection.TOP_LEFT_TO_BOTTOM_RIGHT else RenderLineState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT
		)
	)
}


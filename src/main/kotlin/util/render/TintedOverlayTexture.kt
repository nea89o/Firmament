package moe.nea.firmament.util.render

import me.shedaniel.math.Color
import net.minecraft.client.render.OverlayTexture
import net.minecraft.util.math.ColorHelper
import moe.nea.firmament.util.ErrorUtil

class TintedOverlayTexture : OverlayTexture() {
	companion object {
		val size = 16
	}

	private var lastColor: Color? = null
	fun setColor(color: Color): TintedOverlayTexture {
		val image = ErrorUtil.notNullOr(texture.image, "Disposed TintedOverlayTexture written to") { return this }
		if (color == lastColor) return this
		lastColor = color

		for (i in 0..<size) {
			for (j in 0..<size) {
				if (i < 8) {
					image.setColorArgb(j, i, 0xB2FF0000.toInt())
				} else {
					val k = ((1F - j / 15F * 0.75F) * 255F).toInt()
					image.setColorArgb(j, i, ColorHelper.withAlpha(k, color.color))
				}
			}
		}

		texture.setFilter(false, false)
		texture.setClamp(true)
		texture.upload()
		return this
	}
}

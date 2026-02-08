package moe.nea.firmament.util.render

import me.shedaniel.math.Color
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.ARGB
import moe.nea.firmament.util.ErrorUtil

class TintedOverlayTexture : OverlayTexture() {
	companion object {
		val size = 16
	}

	private var lastColor: Color? = null
	fun setColor(color: Color): TintedOverlayTexture {
		val image = ErrorUtil.notNullOr(texture.pixels, "Disposed TintedOverlayTexture written to") { return this }
		if (color == lastColor) return this
		lastColor = color

		for (i in 0..<size) {
			for (j in 0..<size) {
				if (i < 8) {
					image.setPixel(j, i, 0xB2FF0000.toInt())
				} else {
					val k = ((1F - j / 15F * 0.75F) * 255F).toInt()
					image.setPixel(j, i, ARGB.color(k, color.color))
				}
			}
		}

//		texture.sampler =
//		texture.setFilter(false, false)
//		texture.setClamp(true)
		texture.upload()
		return this
	}
}

package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.ChromaColour
import java.awt.Color

fun ChromaColour.getRGBAWithoutAnimation() =
	Color(ChromaColour.specialToSimpleRGB(toLegacyString()), true)

fun Color.toChromaWithoutAnimation(timeForFullRotationInMillis: Int = 0) =
	ChromaColour.fromRGB(red, green, blue, timeForFullRotationInMillis, alpha)

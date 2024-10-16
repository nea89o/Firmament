package moe.nea.firmament.util.render

import org.joml.Matrix4f
import net.minecraft.client.gui.DrawContext

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return (matrices.peek().positionMatrix.properties() and Matrix4f.PROPERTY_IDENTITY.toInt()) != 0
}

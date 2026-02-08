package moe.nea.firmament.gui.config

import moe.nea.jarvis.api.JarvisHud
import org.joml.Matrix3x2f
import org.joml.Vector2i
import org.joml.Vector2ic
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import moe.nea.firmament.jarvis.JarvisIntegration

@Serializable
data class HudPosition(
	var x: Int,
	var y: Int,
	var scale: Float,
)


data class HudMeta(
    val position: HudPosition,
    private val id: Identifier,
    private val label: Component,
    private val width: Int,
    private val height: Int,
) : JarvisHud, JarvisHud.Scalable {
	override fun getLabel(): Component = label
	override fun getUnscaledWidth(): Int {
		return width
	}

	override fun getUnscaledHeight(): Int {
		return height
	}

	override fun getHudId(): Identifier {
		return id
	}

	override fun getPosition(): Vector2ic {
		return Vector2i(position.x, position.y)
	}

	override fun setPosition(p0: Vector2ic) {
		position.x = p0.x()
		position.y = p0.y()
	}

	override fun isEnabled(): Boolean {
		return true // TODO: this should be actually truthful, if possible
	}

	override fun isVisible(): Boolean {
		return true // TODO: this should be actually truthful, if possible
	}

	override fun getScale(): Float = position.scale

	override fun setScale(newScale: Float) {
		position.scale = newScale
	}

	fun applyTransformations(matrix4f: Matrix3x2f) {
		applyTransformations(JarvisIntegration.jarvis, matrix4f)
	}

}

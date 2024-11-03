package moe.nea.firmament.util.render

import net.minecraft.client.gl.Defines
import net.minecraft.client.gl.ShaderProgramKey
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.DebugInstantiateEvent
import moe.nea.firmament.util.MC

object FirmamentShaders {
	val shaders = mutableListOf<ShaderProgramKey>()

	private fun shader(name: String, format: VertexFormat, defines: Defines): ShaderProgramKey {
		val key = ShaderProgramKey(Firmament.identifier(name), format, defines)
		shaders.add(key)
		return key
	}

	val LINES = RenderPhase.ShaderProgram(shader("core/rendertype_lines", VertexFormats.LINES, Defines.EMPTY))

	@Subscribe
	fun debugLoad(event: DebugInstantiateEvent) {
		shaders.forEach {
			MC.instance.shaderLoader.getOrCreateProgram(it)
		}
	}
}

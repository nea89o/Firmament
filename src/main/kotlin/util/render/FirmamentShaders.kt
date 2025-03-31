package moe.nea.firmament.util.render

import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.CompiledShader
import net.minecraft.client.gl.Defines
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.DebugInstantiateEvent
import moe.nea.firmament.util.MC

object FirmamentShaders {

	@Subscribe
	fun debugLoad(event: DebugInstantiateEvent) {
		// TODO: do i still need to work with shaders like this?
	}
}

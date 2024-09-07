package moe.nea.firmament.util.render

import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.RegisterCustomShadersEvent

object FirmamentShaders {


    private lateinit var _LINES: ShaderProgram
    val LINES = RenderPhase.ShaderProgram({ _LINES })

    @Subscribe
    fun registerCustomShaders(event: RegisterCustomShadersEvent) {
        event.register(
            "firmament_rendertype_lines",
            VertexFormats.LINES,
            { _LINES = it },
        )
    }
}

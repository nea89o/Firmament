package moe.nea.firmament.events

import com.mojang.datafixers.util.Pair
import java.util.function.Consumer
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.VertexFormat
import net.minecraft.resource.ResourceFactory
import moe.nea.firmament.Firmament

data class RegisterCustomShadersEvent(
    val list: MutableList<Pair<ShaderProgram, Consumer<ShaderProgram>>>,
    val resourceFactory: ResourceFactory,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<RegisterCustomShadersEvent>()

    fun register(name: String, vertexFormat: VertexFormat, saver: Consumer<ShaderProgram>) {
        require(name.startsWith("firmament_"))
        try {
            list.add(Pair.of(ShaderProgram(resourceFactory, name, vertexFormat), saver))
        } catch (ex: Exception) {
            Firmament.logger.fatal("Could not load firmament shader $name", ex)
        }
    }
}

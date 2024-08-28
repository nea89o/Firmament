import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer
import moe.nea.firmament.mixins.accessor.sodium.AccessorSodiumWorldRenderer

class SodiumChunkReloader : Runnable {
    override fun run() {
        (SodiumWorldRenderer.instanceNullable() as? AccessorSodiumWorldRenderer)
            ?.renderSectionManager_firmament
            ?.markGraphDirty()
    }
}

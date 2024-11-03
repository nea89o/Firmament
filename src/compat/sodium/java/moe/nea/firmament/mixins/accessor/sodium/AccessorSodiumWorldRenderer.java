package moe.nea.firmament.mixins.accessor.sodium;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SodiumWorldRenderer.class)
@Pseudo
public interface AccessorSodiumWorldRenderer {
	@Accessor(value = "renderSectionManager", remap = false)
	RenderSectionManager getRenderSectionManager_firmament();
}

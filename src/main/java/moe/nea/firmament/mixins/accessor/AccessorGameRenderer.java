package moe.nea.firmament.mixins.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;

@Mixin(GameRenderer.class)
public interface AccessorGameRenderer {
	@Accessor("globalSettingsUniform")
	GlobalSettingsUniform getGlobalSettingsUniform_Firmament();
}

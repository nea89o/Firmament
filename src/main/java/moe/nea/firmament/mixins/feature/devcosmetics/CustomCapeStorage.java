package moe.nea.firmament.mixins.feature.devcosmetics;

import moe.nea.firmament.features.misc.CustomCapes;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public class CustomCapeStorage implements CustomCapes.CapeStorage {
	@Unique
	CustomCapes.CustomCape customCape;

	@Override
	public CustomCapes.@Nullable CustomCape getCape_firmament() {
		return customCape;
	}

	@Override
	public void setCape_firmament(CustomCapes.@Nullable CustomCape customCape) {
		this.customCape = customCape;
	}
}

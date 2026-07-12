package moe.nea.firmament.mixins.owolib;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import org.spongepowered.asm.mixin.Mixin;
import moe.nea.firmament.util.compatloader.CompatLoader;

@CompatLoader.RequireMod(modId = "owo", minVersion = "0.13.0", maxVersion = "0.13.0")
@CompatLoader.RequireMod(modId = "roughlyenoughitems")
@Mixin(targets = "io.wispforest.owo.compat.rei.OwoReiPlugin")
public abstract class OwoReiPluginPatch implements REIClientPlugin {
	// Dummy mixin to add the missing interface implementation and prevent the game from crashing
}

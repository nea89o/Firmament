package moe.nea.firmament.mixins.compat;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.fabric.PluginDetectorImpl;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;


/// Currently owo-lib crashes REI on launch. Some of our users also use owo-lib, and since we are likely the only user of REI, they trace things back to use. This patch blocks out that crash.
///
/// @see <a href="https://github.com/wisp-forest/owo-lib/pull/487">owo-lib#487</a>
@Mixin(PluginDetectorImpl.class)
@Pseudo
public class DontCrashOnOwoPatch {
	@WrapWithCondition(method = "loadPlugin", at = @At(value = "INVOKE", target = "Lme/shedaniel/rei/RoughlyEnoughItemsState;error(Ljava/lang/String;)V"), require = 0)
	private static <P extends REIPlugin<?>> boolean doThing(String reason, @Local EntrypointContainer<REIPluginProvider<?>> container) {
		return !container.getProvider().getMetadata().getId().contains("owo");
	}
}

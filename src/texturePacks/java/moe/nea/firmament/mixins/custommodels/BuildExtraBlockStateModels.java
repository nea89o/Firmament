package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelBaker.class)
public class BuildExtraBlockStateModels {
	@ModifyReturnValue(method = "bake", at = @At("RETURN"))
	private CompletableFuture<ModelBaker.BakedModels> injectMoreBlockModels(CompletableFuture<ModelBaker.BakedModels> original, @Local ModelBaker.BakerImpl baker, @Local(argsOnly = true) Executor executor) {
		Baker b = baker;
		return original.thenCombine(
			CustomBlockTextures.createBakedModels(b, executor),
			(a, _void) -> a
		);
	}
}

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.resource.Resource;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(BlockStatesLoader.class)
public class LoadExtraBlockStates {
	@ModifyExpressionValue(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private static CompletableFuture<Map<Identifier, List<Resource>>> loadExtraModels(
		CompletableFuture<Map<Identifier, List<Resource>>> x,
		@Local(argsOnly = true) Executor executor,
		@Local Function<Identifier, StateManager<Block, BlockState>> stateManagers
	) {
		return x.thenCombineAsync(CustomBlockTextures.getPreparationFuture(), (original, extra) -> {
			CustomBlockTextures.collectExtraBlockStateMaps(extra, original, stateManagers);
			return original;
		}, executor);
	}
}

package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(BlockStateModelLoader.class)
public class LoadExtraBlockStates {
	@ModifyExpressionValue(method = "loadBlockStates", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private static CompletableFuture<Map<Identifier, List<Resource>>> loadExtraModels(
		CompletableFuture<Map<Identifier, List<Resource>>> x,
		@Local(argsOnly = true) Executor executor,
		@Local Function<Identifier, StateDefinition<Block, BlockState>> stateManagers
	) {
		return x.thenCombineAsync(CustomBlockTextures.getPreparationFuture(), (original, extra) -> {
			CustomBlockTextures.collectExtraBlockStateMaps(extra, original, stateManagers);
			return original;
		}, executor);
	}
}

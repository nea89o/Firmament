package moe.nea.firmament.mixins.compat.jade;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.compat.jade.CustomFakeBlockProvider;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.BlockAccessor;

import java.util.List;

@Mixin(HarvestToolProvider.class)
public class EnforceToolDisplayForCustomBlocksInHarvestToolProvider {
	@ModifyExpressionValue(method = "getText", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isToolRequired()Z"))
	private boolean overwriteRequiresTool(boolean original, @Local(argsOnly = true) BlockAccessor accessor) {
		if (CustomFakeBlockProvider.hasCustomBlock(accessor))
			return true;
		return original;
	}

	private static final List<ItemStack> REPLACEABLE_TOOL = List.of(new ItemStack(Blocks.ENCHANTING_TABLE));

	@ModifyExpressionValue(method = "getText", at = @At(value = "INVOKE", target = "Lcom/google/common/cache/Cache;get(Ljava/lang/Object;Ljava/util/concurrent/Callable;)Ljava/lang/Object;"))
	private Object overwriteAvailableTools(Object original, @Local(argsOnly = true) BlockAccessor accessor) {
		var orig = (List<ItemStack>) original;
		if (orig.isEmpty() && CustomFakeBlockProvider.hasCustomBlock(accessor))
			return REPLACEABLE_TOOL;
		return orig;
	}
}

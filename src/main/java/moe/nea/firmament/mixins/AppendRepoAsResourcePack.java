
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.repo.RepoModResourcePack;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesSorter;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ModPackResourcesUtil.class)
public class AppendRepoAsResourcePack {
	@Inject(
		method = "getModResourcePacks",
		at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/resource/pack/ModPackResourcesSorter;getPacks()Ljava/util/List;"),
		require = 0
	)
	private static void onAppendModResourcePack(
		FabricLoader fabricLoader, PackType type, @Nullable String subPath, CallbackInfoReturnable<List<ModResourcePack>> cir,
		@Local ModPackResourcesSorter sorter
	) {
		RepoModResourcePack.Companion.append(sorter);
	}

}


package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.repo.RepoModResourcePack;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackSorter;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ModResourcePackUtil.class)
public class AppendRepoAsResourcePack {
	@Inject(
		method = "getModResourcePacks",
		at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/resource/loader/ModResourcePackSorter;getPacks()Ljava/util/List;"),
		require = 0
	)
	private static void onAppendModResourcePack(
		FabricLoader fabricLoader, ResourceType type, @Nullable String subPath, CallbackInfoReturnable<List<ModResourcePack>> cir,
		@Local ModResourcePackSorter sorter
	) {
		RepoModResourcePack.Companion.append(sorter);
	}

}

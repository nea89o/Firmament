
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.repo.RepoModResourcePack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReloadableResourceManager.class)
public class BandAidResourcePackPatch {

    @ModifyReturnValue(
        method = "getResource",
        at = @At("RETURN")
    )
    private Optional<Resource> injectOurCustomResourcesInCaseExistingMethodsFailed(Optional<Resource> original, @Local(argsOnly = true) Identifier identifier) {
        return original.or(() -> RepoModResourcePack.Companion.createResourceDirectly(identifier));
    }
}

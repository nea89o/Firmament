package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry {
    @ModifyArg(method = "loadTextures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PlayerSkinProvider;loadSkin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;Z)V"))
    public boolean shouldBeSecure(boolean originalSecure) {
        if (Fixes.TConfig.INSTANCE.getFixUnsignedPlayerSkins()) {
            return false;
        }
        return originalSecure;
    }
}

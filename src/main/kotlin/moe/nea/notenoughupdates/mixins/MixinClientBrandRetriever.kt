package moe.nea.notenoughupdates.mixins

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Overwrite
import net.minecraft.client.ClientBrandRetriever

@Mixin(ClientBrandRetriever::class)
class MixinClientBrandRetriever {

private    companion object {
        @JvmStatic
        @Overwrite
        fun getClientModName(): String {
            return "penis"
        }
    }

}

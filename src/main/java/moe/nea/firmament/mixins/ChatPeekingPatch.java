

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatPeekingPatch {

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V", at = @At(value = "HEAD"), index = 4, argsOnly = true)
    public boolean onGetChatHud(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }

    @ModifyExpressionValue(method = "getHeight()I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;isChatFocused()Z"))
    public boolean onGetChatHudHeight(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }

}

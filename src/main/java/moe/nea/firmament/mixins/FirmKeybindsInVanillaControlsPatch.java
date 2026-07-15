

package moe.nea.firmament.mixins;

import moe.nea.firmament.gui.config.KeyBindingHandler;
import moe.nea.firmament.keybindings.FirmamentKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.KeyEntry.class)
public class FirmKeybindsInVanillaControlsPatch {

    @Mutable
    @Shadow
    @Final
    private Button changeButton;

    @Shadow
    @Final
    private KeyMapping key;

    @Shadow
    @Final
    private Button resetButton;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;"))
    public Button.OnPress onInit(Button.OnPress action) {
        var config = FirmamentKeyBindings.INSTANCE.getKeyBindings().get(key);
        if (config == null) return action;
        return button -> {
            ((KeyBindingHandler) config.getHandler())
                .getManagedConfig()
                .showConfigEditor(Minecraft.getInstance().gui.screen());
        };
    }

    @Inject(method = "refreshEntry", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        var config = FirmamentKeyBindings.INSTANCE.getKeyBindings().get(key);
        if (config == null) return;
        resetButton.active = false;
        changeButton.setMessage(Component.translatable("firmament.keybinding.external", config.getValue().format()));
        ci.cancel();
    }

}

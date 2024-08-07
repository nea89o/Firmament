

package moe.nea.firmament.mixins;

import moe.nea.firmament.gui.config.KeyBindingHandler;
import moe.nea.firmament.gui.config.ManagedConfig;
import moe.nea.firmament.keybindings.FirmamentKeyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class FirmKeybindsInVanillaControlsPatch {

    @Mutable
    @Shadow
    @Final
    private ButtonWidget editButton;

    @Shadow
    @Final
    private KeyBinding binding;

    @Shadow
    @Final
    private ButtonWidget resetButton;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    public ButtonWidget.PressAction onInit(ButtonWidget.PressAction action) {
        var config = FirmamentKeyBindings.INSTANCE.getKeyBindings().get(binding);
        if (config == null) return action;
        return button -> {
            ((KeyBindingHandler) config.getHandler())
                .getManagedConfig()
                .showConfigEditor(MinecraftClient.getInstance().currentScreen);
        };
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        var config = FirmamentKeyBindings.INSTANCE.getKeyBindings().get(binding);
        if (config == null) return;
        resetButton.active = false;
        editButton.setMessage(Text.translatable("firmament.keybinding.external", config.value.format()));
        ci.cancel();
    }

}

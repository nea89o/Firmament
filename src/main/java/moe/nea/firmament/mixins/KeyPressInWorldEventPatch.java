

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.events.WorldKeyboardEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Keyboard.class)
public class KeyPressInWorldEventPatch {

    @WrapWithCondition(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"))
    public boolean onKeyBoardInWorld(InputUtil.Key key, long window, int _key, int scancode, int action, int modifiers) {
        var event = WorldKeyboardEvent.Companion.publish(new WorldKeyboardEvent(_key, scancode, modifiers));
		return !event.getCancelled();
	}
}

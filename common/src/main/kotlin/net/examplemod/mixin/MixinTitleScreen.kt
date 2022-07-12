package net.examplemod.mixin

import net.minecraft.client.gui.screens.TitleScreen
import org.objectweb.asm.Opcodes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(TitleScreen::class)
class MixinTitleScreen {
    @Inject(at = [At("HEAD")], method = ["init()V"])
    private fun init(info: CallbackInfo) {
        println("Hello from example architectury common mixin!")
    }

    @Redirect(method = ["render"], at = At("FIELD", target = "minceraftEasterEgg", opcode = Opcodes.GETFIELD))
    private fun nextFloat(t: TitleScreen): Boolean {
        return true
    }

}

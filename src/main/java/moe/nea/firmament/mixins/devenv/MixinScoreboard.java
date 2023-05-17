package moe.nea.firmament.mixins.devenv;

import net.minecraft.scoreboard.Scoreboard;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Scoreboard.class)
public class MixinScoreboard {
    @Redirect(method = "addTeam", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    public void onExistingteam(Logger instance, String s, Object o) {
        // Ignore creations of existing teams
    }
}

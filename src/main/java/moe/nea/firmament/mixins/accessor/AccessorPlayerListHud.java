package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHud.class)
public interface AccessorPlayerListHud {
	@Accessor("footer")
	Text getFooter_firmament();
}

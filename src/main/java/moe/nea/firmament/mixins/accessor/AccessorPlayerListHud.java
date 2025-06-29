package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Comparator;
import java.util.List;

@Mixin(PlayerListHud.class)
public interface AccessorPlayerListHud {

	@Accessor("ENTRY_ORDERING")
	static Comparator<PlayerListEntry> getEntryOrdering() {
		throw new AssertionError();
	}

	@Invoker("collectPlayerEntries")
	List<PlayerListEntry> collectPlayerEntries_firmament();

	@Accessor("footer")
	@Nullable Text getFooter_firmament();

	@Accessor("header")
	@Nullable Text getHeader_firmament();

}

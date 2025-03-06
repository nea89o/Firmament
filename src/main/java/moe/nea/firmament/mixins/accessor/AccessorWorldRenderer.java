package moe.nea.firmament.mixins.accessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.BlockBreakingInfo;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public interface AccessorWorldRenderer {
	@Accessor("blockBreakingProgressions")
	@NotNull
	Long2ObjectMap<SortedSet<BlockBreakingInfo>> getBlockBreakingProgressions_firmament();
}


package moe.nea.firmament.mixins.custommodels;

import net.minecraft.client.item.ItemModelManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemModelManager.class)
public class ApplyHeadModelInItemRenderer {
	// TODO: replace head_model with a condition model (if possible, automatically)
	// TODO: ItemAsset.CODEC should upgrade partials
}

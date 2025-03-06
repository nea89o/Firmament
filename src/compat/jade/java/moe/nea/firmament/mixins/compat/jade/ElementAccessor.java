package moe.nea.firmament.mixins.compat.jade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

@Mixin(Element.class)
public interface ElementAccessor {
	@Accessor("align")
	IElement.Align getAlign_firmament();
}

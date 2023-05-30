package moe.nea.firmament.mixins;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityIdFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityIdFix.class)
public abstract class MixinEntityIdFix extends DataFix {
    @Shadow
    @Final
    private static Map<String, String> RENAMED_ENTITIES;

    public MixinEntityIdFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Inject(method = "makeRule", at = @At("RETURN"), cancellable = true)
    public void onMakeRule(CallbackInfoReturnable<TypeRewriteRule> cir) {
        cir.setReturnValue(TypeRewriteRule.seq(fixTypeEverywhere("EntityIdFix", getInputSchema().findChoiceType(TypeReferences.ENTITY), getOutputSchema().findChoiceType(TypeReferences.ENTITY), dynamicOps -> pair -> ((Pair) pair).mapFirst(string -> RENAMED_ENTITIES.getOrDefault(string, (String) string))), convertUnchecked("Fix Type", getInputSchema().getType(TypeReferences.ITEM_STACK), getOutputSchema().getType(TypeReferences.ITEM_STACK))));
    }
}

package moe.nea.firmament.mixins.compat.citresewn;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import moe.nea.firmament.compat.citresewn.ConditionNBTMixin;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.defaults.cit.conditions.ConditionComponents;
import shcm.shsupercm.fabric.citresewn.defaults.cit.conditions.ConditionNBT;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyGroup;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyKey;
import shcm.shsupercm.fabric.citresewn.pack.format.PropertyValue;

// People are complaining but this really is not my place to fix things

@Mixin(ConditionComponents.class)
@Pseudo
public class MixinConditionComponents {
    @Shadow
    private ComponentType<?> componentType;

    @Shadow(remap = false)
    private ConditionNBT fallbackNBTCheck;
    @Unique
    private String[] pathCheck;
    @Unique
    private int loreInt = -1;

    @Inject(method = "load", at = @At("HEAD"), remap = false)
    public void addExtraAttributeSupport(PropertyKey key, PropertyValue value, PropertyGroup properties, CallbackInfo ci,
                                         @Local(argsOnly = true) LocalRef<PropertyKey> keyRef,
                                         @Local(argsOnly = true) LocalRef<PropertyValue> valueRef) {
        if (!CustomSkyBlockTextures.TConfig.INSTANCE.getEnableLegacyCIT()) return;
        if (!"nbt".equals(key.path())) return;
        if (!value.keyMetadata().startsWith("ExtraAttributes.")) return;
        keyRef.set(new PropertyKey(key.namespace(), "component"));
        valueRef.set(new PropertyValue(
            "minecraft:custom_data" + value.keyMetadata().substring("ExtraAttributes".length()),
            value.value(),
            value.separator(),
            value.position(),
            value.propertiesIdentifier(),
            value.packName()
        ));
        CITResewn.logWarnLoading(properties.messageWithDescriptorOf("NBT condition is not supported since 1.21. THIS IS A FIRMAMENT SPECIAL FEATURE ALLOWING YOU TO CONTINUE TO USE nbt.ExtraAttributes.* PROPERTIES FOR A LIMITED TIME! UPDATE YOUR .PROPERTIES FILES OR SWITCH TO FIRMAMENT CIT (https://github.com/FirmamentMC/CitToFirm)",
                                                                    value.position()));
    }

    @Inject(method = "load",
        at = @At(value = "INVOKE", remap = false, target = "Lshcm/shsupercm/fabric/citresewn/defaults/cit/conditions/ConditionNBT;loadNbtCondition(Lshcm/shsupercm/fabric/citresewn/pack/format/PropertyValue;Lshcm/shsupercm/fabric/citresewn/pack/format/PropertyGroup;[Ljava/lang/String;Ljava/lang/String;)V"),
        remap = false)
    private void onLoadSavePath(PropertyKey key, PropertyValue value, PropertyGroup properties, CallbackInfo ci,
                                @Local String[] path) {
        this.pathCheck = path;
        this.loreInt = -1;
    }

    @Unique
    private boolean matchStringDirect(String directString, CITContext context) {
        return ConditionNBTMixin.invokeDirectConditionNBTStringMatch(fallbackNBTCheck, directString);
    }

    @WrapOperation(method = "test", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), remap = false)
    DataResult fastPathUnsafeNbtComponent(
        Codec instance,
        DynamicOps dynamicOps,
        Object o,
        Operation<DataResult> original) {
        if (o instanceof NbtComponent nbtComponent) {
            return DataResult.success(nbtComponent.getNbt());
        }
        return original.call(instance, dynamicOps, o);
    }

    @Inject(method = "test", at = @At("HEAD"), cancellable = true, remap = false)
    void fastPathDisplayName(CITContext context, CallbackInfoReturnable<Boolean> cir) {
        if (this.componentType == DataComponentTypes.CUSTOM_NAME && pathCheck.length == 0) {
            var displayName = context.stack.getComponents().get(DataComponentTypes.CUSTOM_NAME);
            if (displayName != null) {
                cir.setReturnValue(matchStringDirect((displayName.getString()), context));
            }
        }
        if (this.componentType == DataComponentTypes.LORE && pathCheck.length == 1) {
            var lore = context.stack.getComponents().get(DataComponentTypes.LORE);
            if (lore != null) {
                var loreLines = lore.lines();
                if (pathCheck[0].equals("*")) {
                    for (var loreLine : loreLines) {
                        if (matchStringDirect((loreLine.getString()), context)) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                    cir.setReturnValue(false);
                } else {
                    if (loreInt < 0)
                        loreInt = Integer.parseInt(pathCheck[0]);
                    cir.setReturnValue(0 <= loreInt && loreInt < loreLines.size() &&
                                           matchStringDirect((loreLines.get(loreInt).getString()), context));
                }
            }
        }
    }


}

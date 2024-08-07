
package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.util.customgui.CoordRememberingSlot;
import moe.nea.firmament.util.customgui.CustomGui;
import moe.nea.firmament.util.customgui.HasCustomGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class PatchHandledScreen<T extends ScreenHandler> extends Screen implements HasCustomGui {
    @Shadow
    @Final
    protected T handler;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Unique
    public CustomGui override;
    @Unique
    public boolean hasRememberedSlots = false;

    protected PatchHandledScreen(Text title) {
        super(title);
    }

    @Nullable
    @Override
    public CustomGui getCustomGui_Firmament() {
        return override;
    }

    @Override
    public void setCustomGui_Firmament(@Nullable CustomGui gui) {
        this.override = gui;
    }

    public boolean mouseScrolled_firmament(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return override != null && override.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (override != null) {
            override.onInit();
        }
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (override != null && !override.shouldDrawForeground())
            ci.cancel();
    }


    @Unique
    private Slot didBeforeSlotRender;

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;"))
    private Object beforeSlotRender(DefaultedList instance, int index, Operation<Object> original, @Local(argsOnly = true) DrawContext context) {
        var slot = (Slot) original.call(instance, index);
        if (override != null) {
            didBeforeSlotRender = slot;
            override.beforeSlotRender(context, slot);
        }
        return slot;
    }

    @Inject(method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;size()I"))
    private void afterSlotRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (override != null && didBeforeSlotRender != null) {
            override.afterSlotRender(context, didBeforeSlotRender);
            didBeforeSlotRender = null;
        }
    }

    @Inject(method = "isClickOutsideBounds", at = @At("HEAD"), cancellable = true)
    public void onIsClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        if (override != null) {
            cir.setReturnValue(override.isClickOutsideBounds(mouseX, mouseY));
        }
    }

    @Inject(method = "isPointWithinBounds", at = @At("HEAD"), cancellable = true)
    public void onIsPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (override != null) {
            cir.setReturnValue(override.isPointWithinBounds(x + this.x, y + this.y, width, height, pointX, pointY));
        }
    }

    @Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
    public void onIsPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (override != null) {
            cir.setReturnValue(override.isPointOverSlot(slot, this.x, this.y, pointX, pointY));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void moveSlots(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (override != null) {
            for (Slot slot : handler.slots) {
                if (!hasRememberedSlots) {
                    ((CoordRememberingSlot) slot).rememberCoords_firmament();
                }
                override.moveSlot(slot);
            }
            hasRememberedSlots = true;
        } else {
            if (hasRememberedSlots) {
                for (Slot slot : handler.slots) {
                    ((CoordRememberingSlot) slot).restoreCoords_firmament();
                }
                hasRememberedSlots = false;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "close", cancellable = true)
    private void onVoluntaryExit(CallbackInfo ci) {
        if (override != null) {
            if (!override.onVoluntaryExit())
                ci.cancel();
        }
    }

    @WrapWithCondition(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V"))
    public boolean preventDrawingBackground(HandledScreen instance, DrawContext drawContext, float delta, int mouseX, int mouseY) {
        if (override != null) {
            override.render(drawContext, delta, mouseX, mouseY);
        }
        return override == null;
    }

    @WrapOperation(
        method = "mouseClicked",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
    public boolean overrideMouseClicks(HandledScreen instance, double mouseX, double mouseY, int button,
                                       Operation<Boolean> original) {
        if (override != null) {
            if (override.mouseClick(mouseX, mouseY, button))
                return true;
        }
        return original.call(instance, mouseX, mouseY, button);
    }
}

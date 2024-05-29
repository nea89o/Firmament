/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.apis.ingame.FirmamentCustomPayload;
import moe.nea.firmament.events.FirmamentCustomPayloadEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class CustomPayloadEventDispatcher {
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleFirmamentParsedPayload(CustomPayload payload, CallbackInfo ci) {
        if (payload instanceof FirmamentCustomPayload customPayload) {
            FirmamentCustomPayloadEvent.Companion.publish(new FirmamentCustomPayloadEvent(customPayload));
            ci.cancel();
        }
    }
}

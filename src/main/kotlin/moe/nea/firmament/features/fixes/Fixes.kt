/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.fixes

import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object Fixes : FirmamentFeature {
    override val identifier: String
        get() = "fixes"

    object TConfig : ManagedConfig(identifier) {
        val fixUnsignedPlayerSkins by toggle("player-skins") { true }
        val autoSprint by toggle("auto-sprint") { false }
    }

    override val config: ManagedConfig
        get() = TConfig

    fun handleIsPressed(
        keyBinding: KeyBinding,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        if (keyBinding === MinecraftClient.getInstance().options.sprintKey && TConfig.autoSprint && MC.player?.isSprinting != true)
            cir.returnValue = true
    }

    override fun onLoad() {
    }
}

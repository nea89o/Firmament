/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.fixes

import moe.nea.jarvis.api.Point
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Arm
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.errorBoundary

object Fixes : FirmamentFeature {
    override val identifier: String
        get() = "fixes"

    object TConfig : ManagedConfig(identifier) {
        val fixUnsignedPlayerSkins by toggle("player-skins") { true }
        var autoSprint by toggle("auto-sprint") { false }
        val autoSprintKeyBinding by keyBindingWithDefaultUnbound("auto-sprint-keybinding")
        val autoSprintHud by position("auto-sprint-hud", 80, 10) { Point(0.0, 1.0) }
        val peekChat by keyBindingWithDefaultUnbound("peek-chat")
        val useClientSidedHandedness by toggle("clientside-lefthand") { true }
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

    fun isLeftHandedHook(entity: PlayerEntity, cit: CallbackInfoReturnable<Arm>) = errorBoundary {
        if (TConfig.useClientSidedHandedness && entity.isMainPlayer) {
            cit.returnValue = MC.instance.options.mainArm.value
        }
    }

    override fun onLoad() {
        WorldKeyboardEvent.subscribe {
            if (it.matches(TConfig.autoSprintKeyBinding)) {
                TConfig.autoSprint = !TConfig.autoSprint
            }
        }
        HudRenderEvent.subscribe {
            if (!TConfig.autoSprintKeyBinding.isBound) return@subscribe
            it.context.matrices.push()
            TConfig.autoSprintHud.applyTransformations(it.context.matrices)
            it.context.drawText(
                MC.font, Text.translatable(
                    if (TConfig.autoSprint)
                        "firmament.fixes.auto-sprint.on"
                    else if (MC.player?.isSprinting == true)
                        "firmament.fixes.auto-sprint.sprinting"
                    else
                        "firmament.fixes.auto-sprint.not-sprinting"
                ), 0, 0, -1, false
            )
            it.context.matrices.pop()
        }
    }

    fun shouldPeekChat(): Boolean {
        return TConfig.peekChat.isPressed(atLeast = true)
    }
}

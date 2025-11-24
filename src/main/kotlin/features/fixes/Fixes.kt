package moe.nea.firmament.features.fixes

import org.joml.Vector2i
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.client.Minecraft
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.tr

object Fixes {
	val identifier: String
		get() = "fixes"

	@Config
	object TConfig : ManagedConfig(identifier, Category.MISC) { // TODO: split this config
		val fixUnsignedPlayerSkins by toggle("player-skins") { true }
		var autoSprint by toggle("auto-sprint") { false }
		val autoSprintKeyBinding by keyBindingWithDefaultUnbound("auto-sprint-keybinding")
		val autoSprintUnderWater by toggle("auto-sprint-underwater") { true }
		var autoSprintHudToggle by toggle("auto-sprint-hud-toggle") { true }
		val autoSprintHud by position("auto-sprint-hud", 80, 10) { Vector2i() }
		val peekChat by keyBindingWithDefaultUnbound("peek-chat")
		val peekChatScroll by toggle("peek-chat-scroll") { false }
		val hidePotionEffects by toggle("hide-mob-effects") { false }
		val hidePotionEffectsHud by toggle("hide-potion-effects-hud") { false }
		val noHurtCam by toggle("disable-hurt-cam") { false }
		val hideSlotHighlights by toggle("hide-slot-highlights") { false }
		val hideRecipeBook by toggle("hide-recipe-book") { false }
		val hideOffHand by toggle("hide-off-hand") { false }
	}

	fun handleIsPressed(
        keyBinding: KeyMapping,
        cir: CallbackInfoReturnable<Boolean>
	) {
		if (keyBinding !== Minecraft.getInstance().options.keySprint) return
		if (!TConfig.autoSprint) return
		val player = MC.player ?: return
		if (player.isSprinting) return
		if (!TConfig.autoSprintUnderWater && player.isInWater) return
		cir.returnValue = true
	}

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.autoSprintKeyBinding.isBound || !TConfig.autoSprintHudToggle) return
		it.context.pose().pushMatrix()
		TConfig.autoSprintHud.applyTransformations(it.context.pose())
		it.context.drawString(
			MC.font, (
				if (MC.player?.isSprinting == true) {
					Component.translatable("firmament.fixes.auto-sprint.sprinting")
				} else if (TConfig.autoSprint) {
					if (!TConfig.autoSprintUnderWater && MC.player?.isInWater == true)
						tr("firmament.fixes.auto-sprint.under-water", "In Water")
					else
						Component.translatable("firmament.fixes.auto-sprint.on")
				} else {
					Component.translatable("firmament.fixes.auto-sprint.not-sprinting")
				}
				), 0, 0, -1, true
		)
		it.context.pose().popMatrix()
	}

	@Subscribe
	fun onWorldKeyboard(it: WorldKeyboardEvent) {
		if (it.matches(TConfig.autoSprintKeyBinding)) {
			TConfig.autoSprint = !TConfig.autoSprint
		}
	}

	fun shouldPeekChat(): Boolean {
		return TConfig.peekChat.isPressed(atLeast = true)
	}

	fun shouldScrollPeekedChat(): Boolean {
		return TConfig.peekChatScroll
	}
}
